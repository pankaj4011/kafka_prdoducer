package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.controller.LibraryEventsController;
import com.learnkafka.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.requests.ProduceRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class LibraryEventsProducer {

    @Value("${spring.kafka.topic}")
    private String topic;

    private final KafkaTemplate<Integer, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public LibraryEventsProducer(KafkaTemplate<Integer, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public CompletableFuture<SendResult<Integer, String>> sendLibraryEvents(LibraryEvent libraryEvent) throws JsonProcessingException {
        var key = libraryEvent.libraryEventId();
        var value = objectMapper.writeValueAsString(libraryEvent);
        var completableFuture = kafkaTemplate.send(topic, value);
        return completableFuture.whenComplete((sendResult, throwable) -> {
            if (throwable != null) {
                log.error("Error sending the message with key {} and the exception {}", key, throwable.getMessage());
            } else {
                log.info("Message sent successfully for the key {} and the value {}, partition {} ", key, value, sendResult.getRecordMetadata().partition());
            }
        });
    }

    public ProducerRecord<Integer,String> sendLibraryEvents_approach2(LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        var key = libraryEvent.libraryEventId();
        var value = objectMapper.writeValueAsString(libraryEvent);
       return  kafkaTemplate.send(topic,value).get(1, TimeUnit.SECONDS).getProducerRecord();
    }

    public ProducerRecord<Integer,String> sendLibraryEvents_approach3(LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException {
        var key = libraryEvent.libraryEventId();
        var value = objectMapper.writeValueAsString(libraryEvent);

        List<Header> recordHeaders = List.of(new RecordHeader("event-source", "scanner".getBytes()));

        ProducerRecord<Integer,String> record = new ProducerRecord<>(topic,null,key,value,recordHeaders);
        kafkaTemplate.send(record);
        return  kafkaTemplate.send(topic,value).get().getProducerRecord();
    }
}

