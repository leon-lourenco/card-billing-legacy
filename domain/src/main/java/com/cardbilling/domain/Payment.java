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

@Entity
@Table(name = "payments")
public class Payment {

    public enum Source {
        INTERNAL, EXTERNAL_RECONCILIATION
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Source source;

    /** Set when source is EXTERNAL_RECONCILIATION - the statement line this payment came from. */
    @Column(name = "external_reference")
    private String externalReference;

    protected Payment() {
    }

    public Payment(Invoice invoice, long amountCents, LocalDateTime paidAt, Source source, String externalReference) {
        this.invoice = invoice;
        this.amountCents = amountCents;
        this.paidAt = paidAt;
        this.source = source;
        this.externalReference = externalReference;
    }

    public Long getId() {
        return id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public Source getSource() {
        return source;
    }

    public String getExternalReference() {
        return externalReference;
    }
}
