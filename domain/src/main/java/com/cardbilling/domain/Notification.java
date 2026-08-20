package com.cardbilling.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/** One notification dispatch attempt - requested here, updated to SENT/FAILED once the consumer processes it. */
@Entity
@Table(name = "notifications")
public class Notification {

    public enum Channel {
        EMAIL, SMS
    }

    public enum Stage {
        REMINDER_D5, REMINDER_D15, FORMAL_NOTICE_D30
    }

    public enum Status {
        REQUESTED, SENT, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Stage stage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    protected Notification() {
    }

    public Notification(Customer customer, Invoice invoice, Channel channel, Stage stage) {
        this.customer = customer;
        this.invoice = invoice;
        this.channel = channel;
        this.stage = stage;
        this.status = Status.REQUESTED;
        this.requestedAt = LocalDateTime.now();
    }

    public void markSent() {
        this.status = Status.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = Status.FAILED;
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public Channel getChannel() {
        return channel;
    }

    public Stage getStage() {
        return stage;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}
