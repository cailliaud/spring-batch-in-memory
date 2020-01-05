package fr.cailliaud.batch.configuration;


import fr.cailliaud.batch.pojo.Hero;
import fr.cailliaud.batch.pojo.Race;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.*;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.util.List;



@Configuration
@EnableBatchProcessing
@Slf4j
public class BatchConfiguration {

    @Bean
    public BatchConfigurer batchConfigurer() {
        return new CustomBatchConfigurer();
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
        @Qualifier("itemReadListener") ItemReadListener itemReadListener,
        @Qualifier("heroReader") ItemReader<Hero> heroReader,
        @Qualifier("heroProcessor") ItemProcessor<Hero, Hero> heroProcessor,
        @Qualifier("heroWriter") ItemWriter<Hero> heroWriter
    ){
        return this.stepBuilders.get("heroLoad")
            .<Hero, Hero> chunk(2)
            .listener(itemReadListener)
            .reader(heroReader)
            .processor(heroProcessor)
            .writer(heroWriter)
            .build();
    }

    @Bean
    public ItemReadListener itemReadListener(){
        return new ItemReadListener() {
            @Override public void beforeRead() {

            }

            @Override public void afterRead(Object item) {

            }

            @Override public void onReadError(Exception ex) {

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
    public ItemWriter<Hero> heroWriter(){
        return new ItemWriter<Hero>() {
            @Override public void write(List<? extends Hero> items) throws Exception {
                items.forEach(item -> log.info(item.toString()));
            }
        };
    }
}
