package com.cardbilling.domain;

import java.time.LocalDate;
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
@Table(name = "invoices")
public class Invoice {

    public enum Status {
        OPEN, CLOSED, PAID, OVERDUE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    /** The billing cycle this invoice covers, e.g. "2026-08". */
    @Column(name = "reference_month", nullable = false)
    private String referenceMonth;

    @Column(name = "closing_date", nullable = false)
    private LocalDate closingDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "total_amount_cents", nullable = false)
    private long totalAmountCents;

    @Column(name = "interest_applied_cents", nullable = false)
    private long interestAppliedCents;

    /** Guards interest accrual idempotency - null until the first accrual run touches this invoice. */
    @Column(name = "last_interest_accrual_date")
    private LocalDate lastInterestAccrualDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    protected Invoice() {
    }

    public Invoice(Card card, String referenceMonth, LocalDate closingDate, LocalDate dueDate, long totalAmountCents) {
        this.card = card;
        this.referenceMonth = referenceMonth;
        this.closingDate = closingDate;
        this.dueDate = dueDate;
        this.totalAmountCents = totalAmountCents;
        this.interestAppliedCents = 0L;
        this.status = Status.CLOSED;
    }

    public void applyInterest(long amountCents, LocalDate accrualDate) {
        this.interestAppliedCents += amountCents;
        this.lastInterestAccrualDate = accrualDate;
    }

    public void markOverdue() {
        this.status = Status.OVERDUE;
    }

    public void markPaid() {
        this.status = Status.PAID;
    }

    public Long getId() {
        return id;
    }

    public Card getCard() {
        return card;
    }

    public String getReferenceMonth() {
        return referenceMonth;
    }

    public LocalDate getClosingDate() {
        return closingDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public long getTotalAmountCents() {
        return totalAmountCents;
    }

    public long getInterestAppliedCents() {
        return interestAppliedCents;
    }

    public LocalDate getLastInterestAccrualDate() {
        return lastInterestAccrualDate;
    }

    public Status getStatus() {
        return status;
    }
}
