package com.cardbilling.notification;

import com.cardbilling.domain.Customer;
import com.cardbilling.domain.Invoice;
import com.cardbilling.domain.Notification;
import com.cardbilling.domain.repository.NotificationRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Records a notification request and publishes it to Kafka. The record is written before the
 * publish, not inside the same outbox-style guarantee used in pix-payment-gateway - a crash
 * between the two leaves a REQUESTED row nothing ever picks up. That gap is deliberate: this is
 * the legacy side of the portfolio, and an imperfect notification path is part of what the
 * modernized version is meant to fix later, not something to solve here.
 */
@Component
public class NotificationRequestPublisher {

    public static final String TOPIC = "notification.requested";

    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public NotificationRequestPublisher(NotificationRepository notificationRepository,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.notificationRepository = notificationRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void request(Customer customer, Invoice invoice, Notification.Channel channel, Notification.Stage stage) {
        Notification notification = new Notification(customer, invoice, channel, stage);
        notificationRepository.save(notification);
        kafkaTemplate.send(TOPIC, notification.getId().toString());
    }
}
