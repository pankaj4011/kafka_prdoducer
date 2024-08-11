package com.learnkafka.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.util.TestUtil;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.KafkaUtils;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import scala.Int;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = "library_events")
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
class LibraryEventsControllerIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    EmbeddedKafkaBroker kafkaBroker;

    @Autowired
    ObjectMapper objectMapper;

    Consumer<Integer,String> consumer;

    @BeforeEach
    void setUp(){
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", kafkaBroker));
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"latest");
        consumer = new DefaultKafkaConsumerFactory<Integer,String>(configs,new IntegerDeserializer(),new StringDeserializer()).createConsumer();
        kafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void teatDown(){
        consumer.close();
    }

    @Test
    void postLibraryEvent(){

        HttpHeaders httpHeaders = new org.springframework.http.HttpHeaders();
        httpHeaders.set("content-type", MediaType.APPLICATION_JSON.toString());
        var httpEntity = new HttpEntity<>(TestUtil.libraryEventRecord(),httpHeaders);

        var responseEntity = restTemplate.exchange("/v1/libraryevent", HttpMethod.POST,httpEntity, LibraryEvent.class);

        assertEquals(HttpStatus.CREATED,responseEntity.getStatusCode());

        ConsumerRecords<Integer, String> consumerRecord  = KafkaTestUtils.getRecords(consumer);

        consumerRecord.forEach(record -> {
            var libraryEvent = TestUtil.parseLibraryEventRecord(objectMapper,record.value());
                assertEquals(libraryEvent, TestUtil.libraryEventRecord());
        });
    }

}