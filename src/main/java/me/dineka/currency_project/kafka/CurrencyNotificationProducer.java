package me.dineka.currency_project.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CurrencyNotificationProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public CurrencyNotificationProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCurrencyCreated(String currencyCode, String currencyName) {
        String message = String.format("Добавлена валюта: Код валюты = %s, название валюты = %s", currencyCode, currencyName);
        kafkaTemplate.send("currency-created-topic", currencyCode, message);
    }

}
