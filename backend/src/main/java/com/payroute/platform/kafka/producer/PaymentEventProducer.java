package com.payroute.platform.kafka.producer;

import com.payroute.platform.common.util.JsonUtils;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);
    private static final String TOPIC = "payment.events";

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public PaymentEventProducer(@Autowired(required = false) KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishEvent(String key, String eventType, Map<String, Object> payload) {
        if (kafkaTemplate == null) {
            log.debug("KafkaTemplate not configured; skipping event publication for {}", eventType);
            return;
        }

        try {
            Map<String, Object> message = Map.of(
                    "eventType", eventType,
                    "timestamp", System.currentTimeMillis(),
                    "payload", payload
            );
            String json = JsonUtils.toJson(message);

            kafkaTemplate.send(TOPIC, key, json).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.warn("Failed to publish Kafka event {} to topic {}: {}", eventType, TOPIC, ex.getMessage());
                } else {
                    log.info("Successfully published Kafka event [{}] for key [{}]", eventType, key);
                }
            });
        } catch (Exception e) {
            log.warn("Could not dispatch Kafka event: {}", e.getMessage());
        }
    }
}
