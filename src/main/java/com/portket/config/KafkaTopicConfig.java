package com.portket.config;

import com.portket.constant.KafkaTopics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin.NewTopics;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopics testTopic() {
        return new NewTopics(
                TopicBuilder.name(KafkaTopics.TEST).partitions(1).replicas(1).build(),
                TopicBuilder.name(KafkaTopics.INSTRUMENT_BUILD).partitions(1).replicas(1).build(),
                TopicBuilder.name(KafkaTopics.INSTRUMENT_BUILD_SUCCESS).partitions(1).replicas(1).build(),
                TopicBuilder.name(KafkaTopics.INSTRUMENT_BUILD_FAILURE).partitions(1).replicas(1).build()
        );
    }
}
