package com.cardbilling.domain.repository;

import com.cardbilling.domain.Invoice;
import com.cardbilling.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Idempotency guard: has this invoice already been notified at this escalation stage? */
    boolean existsByInvoiceAndStage(Invoice invoice, Notification.Stage stage);
}
