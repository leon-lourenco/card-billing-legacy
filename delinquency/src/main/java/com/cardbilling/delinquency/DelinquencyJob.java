package com.cardbilling.delinquency;

import com.cardbilling.domain.Customer;
import com.cardbilling.domain.Invoice;
import com.cardbilling.domain.Notification;
import com.cardbilling.domain.repository.InvoiceRepository;
import com.cardbilling.domain.repository.NotificationRepository;
import com.cardbilling.notification.NotificationRequestPublisher;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Finds every invoice still unpaid past its due date, marks it overdue, and escalates
 * notification through three fixed stages based on how many days late it is: a reminder at
 * D+5, a second reminder at D+15, a formal notice at D+30. Each customer/invoice/stage
 * combination is only ever notified once - {@link NotificationRepository#existsByInvoiceAndStage}
 * is the idempotency guard that makes a rerun on the same day harmless.
 */
@Component
public class DelinquencyJob {

    private final InvoiceRepository invoiceRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationRequestPublisher notificationRequestPublisher;

    public DelinquencyJob(InvoiceRepository invoiceRepository, NotificationRepository notificationRepository,
            NotificationRequestPublisher notificationRequestPublisher) {
        this.invoiceRepository = invoiceRepository;
        this.notificationRepository = notificationRepository;
        this.notificationRequestPublisher = notificationRequestPublisher;
    }

    @Transactional
    public int checkFor(LocalDate today) {
        List<Invoice> overdue = invoiceRepository.findByStatusNotAndDueDateBefore(Invoice.Status.PAID, today);

        int notifiedCount = 0;
        for (Invoice invoice : overdue) {
            invoice.markOverdue();
            invoiceRepository.save(invoice);

            Notification.Stage stage = stageFor(ChronoUnit.DAYS.between(invoice.getDueDate(), today));
            if (stage == null || notificationRepository.existsByInvoiceAndStage(invoice, stage)) {
                continue;
            }

            Customer customer = invoice.getCard().getAccount().getCustomer();
            notificationRequestPublisher.request(customer, invoice, Notification.Channel.EMAIL, stage);
            notificationRequestPublisher.request(customer, invoice, Notification.Channel.SMS, stage);
            notifiedCount++;
        }
        return notifiedCount;
    }

    private Notification.Stage stageFor(long daysLate) {
        if (daysLate >= 30) {
            return Notification.Stage.FORMAL_NOTICE_D30;
        }
        if (daysLate >= 15) {
            return Notification.Stage.REMINDER_D15;
        }
        if (daysLate >= 5) {
            return Notification.Stage.REMINDER_D5;
        }
        return null;
    }
}
