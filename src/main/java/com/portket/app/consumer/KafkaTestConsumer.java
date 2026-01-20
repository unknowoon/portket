package com.portket.app.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaTestConsumer {

    @KafkaListener(topics = "test", groupId = "portket")
    public void listenTest(String message) {
        log.info("[Consumer test] message: from topic: 'test', message: {}]", message);
    }
}
