package com.cardbilling.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "card_transactions")
public class CardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    /** Null until invoice-closing aggregates this transaction into a billing cycle. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(name = "merchant_name", nullable = false)
    private String merchantName;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    protected CardTransaction() {
    }

    public CardTransaction(Card card, String merchantName, long amountCents, LocalDateTime transactionDate) {
        this.card = card;
        this.merchantName = merchantName;
        this.amountCents = amountCents;
        this.transactionDate = transactionDate;
    }

    public void assignToInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Long getId() {
        return id;
    }

    public Card getCard() {
        return card;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
}
