package com.example.kafka;

import com.example.kafka.listener.MessageListener;
import com.example.kafka.producer.MessageProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = "${kafka.topic.name}")
class KafkaDemoApplicationTests {

    @Autowired
    private MessageProducer producer;

    @Test
    void contextLoads() {
    }

    @Test
    void producerSendsMessage() throws InterruptedException {
        producer.send("hello kafka");
        // Give the listener time to process
        TimeUnit.SECONDS.sleep(1);
    }
}
