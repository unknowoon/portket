package com.portket.app.controller;

import com.portket.app.dto.MessageSenderInstrumentBuildInput;
import com.portket.constant.KafkaTopics;
import com.portket.util.system.KafkaProducerUtil;
import com.nimbusds.jose.shaded.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class KafkaTestController {
    private final KafkaProducerUtil kafkaProducerUtil;

    @PostMapping
    public ResponseEntity<String> sendMessage(@RequestParam String topic, @RequestBody String message) {
        kafkaProducerUtil.sendMessage(topic, message);
        return ResponseEntity.status(HttpStatus.CREATED).body("Message sent successfully. Topic: " + topic + ", Message: " + message);
    }

    @PostMapping("/instrument-build")
    public ResponseEntity<String> sendInstrumentBuildMessage(@RequestBody MessageSenderInstrumentBuildInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sendMessage(input));
    }

    /**
     * 저도 알아요 컨트롤러에 로직이 있으면 안되죠
     */
    private String sendMessage(MessageSenderInstrumentBuildInput input) {

        final String key = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        final String message = new Gson().toJson(input);

        kafkaProducerUtil.sendMessageWithKey(KafkaTopics.INSTRUMENT_BUILD, key, message);
        return "Message sent successfully. Topic: 'instrument-build', Message: " + message;
    }
}
