package fr.cailliaud.batch.configuration;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfiguration {

    @Bean(name="businessDataSource")
    @ConfigurationProperties(prefix = "spring.datasource-business")
    public DataSource businessDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name="batchDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource-batch")
    public DataSource batchDataSource() {
        return DataSourceBuilder.create().build();
    }
}
