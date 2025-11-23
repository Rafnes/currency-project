package me.dineka.currency_project.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CurrencyNotificationConsumer {
    @KafkaListener(topics = "currency-created-topic", groupId = "my-group")
    public void consumeCurrencyMessage(String message) {
        log.info("Получено сообщение : " + message);
    }
}
