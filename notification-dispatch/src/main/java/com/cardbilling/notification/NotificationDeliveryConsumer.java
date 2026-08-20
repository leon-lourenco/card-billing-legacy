package com.cardbilling.notification;

import com.cardbilling.domain.Customer;
import com.cardbilling.domain.Invoice;
import com.cardbilling.domain.Notification;
import com.cardbilling.domain.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumes notification requests and simulates delivery - no real Twilio/SendGrid account, just
 * a log line standing in for "the message left the building" plus the same traceable history
 * row every other job in this system writes.
 */
@Component
public class NotificationDeliveryConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryConsumer.class);

    private final NotificationRepository notificationRepository;

    public NotificationDeliveryConsumer(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Transactional so customer/invoice, both lazy associations, stay resolvable through the
     * whole method - without it, the session used by findById closes as soon as it returns and
     * touching either association below throws LazyInitializationException.
     */
    @Transactional
    @KafkaListener(topics = NotificationRequestPublisher.TOPIC, groupId = "notification-dispatch")
    public void onNotificationRequested(String notificationId) {
        Notification notification = notificationRepository.findById(Long.valueOf(notificationId)).orElse(null);
        if (notification == null) {
            log.warn("Notification {} not found - skipping", notificationId);
            return;
        }

        Customer customer = notification.getCustomer();
        Invoice invoice = notification.getInvoice();
        log.info("[MOCK {}] To {} ({}): invoice #{} ({}) is at stage {}",
                notification.getChannel(), customer.getFullName(), contactFor(notification, customer),
                invoice.getId(), invoice.getReferenceMonth(), notification.getStage());

        notification.markSent();
        notificationRepository.save(notification);
    }

    private String contactFor(Notification notification, Customer customer) {
        return notification.getChannel() == Notification.Channel.EMAIL
                ? customer.getEmail()
                : customer.getPhoneNumber();
    }
}
