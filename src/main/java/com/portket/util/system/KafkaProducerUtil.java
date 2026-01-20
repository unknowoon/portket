package com.portket.util.system;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KafkaProducerUtil {
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 간단 메서드: 메시지를 발행할 때
     */
    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }

    /**
     * 키/값 구조로 보낼 때
     */
    public void sendMessageWithKey(String topic, String key, String message) {
        kafkaTemplate.send(topic, key, message);
    }

    /**
     * 헤더를 포함해 메시지를 보낼 때
     */
    public void sendMessageWithHeaders(String topic, String key, String message, Map<String, String> headers) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);

        // Map<String, String> 형태의 headers를 순회하며 ProducerRecord에 추가
        headers.forEach((headerKey, headerValue) -> record.headers()
                .add(new RecordHeader(
                        headerKey,
                        headerValue.getBytes(StandardCharsets.UTF_8)
                )
        ));

        kafkaTemplate.send(record);
    }
}
