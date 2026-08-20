package com.cardbilling.domain;

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
@Table(name = "cards")
public class Card {

    public enum Status {
        ACTIVE, BLOCKED, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "card_number_masked", nullable = false)
    private String cardNumberMasked;

    @Column(name = "credit_limit_cents", nullable = false)
    private long creditLimitCents;

    /** Day of month the billing cycle closes on, e.g. 5 -> invoices close on the 5th. */
    @Column(name = "billing_cycle_day", nullable = false)
    private int billingCycleDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    protected Card() {
    }

    public Card(Account account, String cardNumberMasked, long creditLimitCents, int billingCycleDay) {
        this.account = account;
        this.cardNumberMasked = cardNumberMasked;
        this.creditLimitCents = creditLimitCents;
        this.billingCycleDay = billingCycleDay;
        this.status = Status.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public String getCardNumberMasked() {
        return cardNumberMasked;
    }

    public long getCreditLimitCents() {
        return creditLimitCents;
    }

    public int getBillingCycleDay() {
        return billingCycleDay;
    }

    public Status getStatus() {
        return status;
    }
}
