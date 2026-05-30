package com.magenta.banks.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.admin.NewTopic;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic productTopic() {
        return TopicBuilder.name("magenta.banks.product.v1").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic preapprovalTopic() {
        return TopicBuilder.name("magenta.banks.preapproval.v1").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic appraisalTopic() {
        return TopicBuilder.name("magenta.banks.appraisal.v1").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic simulationTopic() {
        return TopicBuilder.name("magenta.banks.simulation.v1").partitions(6).replicas(1).build();
    }
}
