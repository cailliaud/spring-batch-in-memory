package fr.cailliaud.batch.configuration;


import fr.cailliaud.batch.pojo.Hero;
import fr.cailliaud.batch.pojo.Race;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.UnmarshallingFailureException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.xml.sax.SAXParseException;

import javax.sql.DataSource;
import java.util.List;



@Configuration
@EnableBatchProcessing
@Slf4j
public class BatchConfiguration {

    @Autowired
    @Qualifier("batchDataSource")
    DataSource batchDataSource;

    @Bean
    BatchConfigurer configurer(){
        return new DefaultBatchConfigurer(batchDataSource);
    }

    @Autowired
    private JobBuilderFactory jobBuilders;

    @Autowired
    private StepBuilderFactory stepBuilders;

    @Value(value="${batch.file}")
    private String filePath;

    @Bean
    public Job heroJob(@Qualifier("heroLoad") Step heroLoad){
        return this.jobBuilders.get("heroJob")
            .start(heroLoad)
            .build();
    }

    @Bean
    public Step heroLoad(
        @Qualifier("heroSkipListener") SkipListener heroSkipListener,
        @Qualifier("heroReader") ItemReader<Hero> heroReader,
        @Qualifier("heroProcessor") ItemProcessor<Hero, Hero> heroProcessor,
        @Qualifier("heroWriter") ItemWriter<Hero> heroWriter
    ){
        return this.stepBuilders.get("heroLoad")
            .<Hero, Hero> chunk(2)
            .reader(heroReader)
            .processor(heroProcessor)
            .writer(heroWriter)
            .faultTolerant()
            .skip(UnmarshallingFailureException.class)
            .skipLimit(3)
            .listener(heroSkipListener)
            .build();
    }

    @Bean
    public SkipListener<Hero,Hero> heroSkipListener(){
      return new SkipListener<Hero,Hero>() {
          @Override public void onSkipInRead(Throwable t) {

              if(t instanceof UnmarshallingFailureException){
                  UnmarshallingFailureException marshallExc = (UnmarshallingFailureException)t;

                  Throwable rootCause = marshallExc.getRootCause();
                  log.error("Exception occred reading item : {}",rootCause.getClass().getName());
                  if(log.isDebugEnabled()){
                      log.debug("Root cause : ",rootCause);
                  }else{
                      log.warn("Root cause : {}",rootCause.getMessage());
                  }

              }else{
                  log.error("Error occcured in read phase.",t.getCause());
              }

          }

          @Override public void onSkipInWrite(Hero item, Throwable t) {

          }

          @Override public void onSkipInProcess(Hero item, Throwable t) {

          }
      };
    }

    @Bean
    public StaxEventItemReader<Hero> heroReader(@Qualifier("heroMarshaller") Jaxb2Marshaller heroMarshaller,ApplicationContext ctx) throws Exception {
        StaxEventItemReader<Hero> staxEventItemReader = new StaxEventItemReader<>();
        Resource resource = ctx.getResource(filePath);
        staxEventItemReader.setResource(resource);
        staxEventItemReader.setFragmentRootElementName("hero");
        staxEventItemReader.setStrict(true);
        staxEventItemReader.setUnmarshaller(heroMarshaller);
        return staxEventItemReader;

    }

    @Bean
    public Jaxb2Marshaller heroMarshaller() throws Exception {
        Jaxb2Marshaller heroMarshaller = new Jaxb2Marshaller();
        heroMarshaller.setClassesToBeBound(Hero.class);
        heroMarshaller.setSchema(new ClassPathResource("/schemas/hero.xsd"));
        heroMarshaller.afterPropertiesSet();
        return heroMarshaller;
    }

    @Bean
    public ItemProcessor<Hero, Hero> heroProcessor(){
        return new ItemProcessor<Hero, Hero>() {
            @Override public Hero process(Hero item) throws Exception {
                if(item.getRace().equals(Race.ELF)){
                    log.debug("Elf race not yet implemented -> become human.");
                    return new Hero(item.getName(),Race.HUMAN);
                }else{
                    return item;
                }
            }
        };
    }

    @Bean
    public JdbcBatchItemWriter<Hero> heroWriter(@Qualifier("businessDataSource") DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Hero>()
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .sql("INSERT INTO heroes (name, race) VALUES (:name, :race.label)")
            .dataSource(dataSource)
            .build();
    }
}
