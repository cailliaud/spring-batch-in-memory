package fr.cailliaud.batch.configuration;

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;

public class CustomBatchConfigurer extends DefaultBatchConfigurer {

    // This would reside in your BatchConfigurer implementation
    @Override
    protected JobRepository createJobRepository() throws Exception {
        MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean();
        factory.setTransactionManager(getTransactionManager());
        return factory.getObject();
    }
}
