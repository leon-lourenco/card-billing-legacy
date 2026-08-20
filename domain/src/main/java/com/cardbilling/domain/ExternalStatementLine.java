package com.cardbilling.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/** One raw row from an ingested external statement CSV, before reconciliation attempts a match. */
@Entity
@Table(name = "external_statement_lines")
public class ExternalStatementLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_reference", nullable = false, unique = true)
    private String externalReference;

    @Column(name = "customer_document_number", nullable = false)
    private String customerDocumentNumber;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(name = "statement_date", nullable = false)
    private LocalDate statementDate;

    @Column(name = "raw_line", nullable = false)
    private String rawLine;

    @Column(name = "ingested_at", nullable = false)
    private LocalDateTime ingestedAt;

    @Column(nullable = false)
    private boolean matched;

    protected ExternalStatementLine() {
    }

    public ExternalStatementLine(String externalReference, String customerDocumentNumber, long amountCents,
            LocalDate statementDate, String rawLine) {
        this.externalReference = externalReference;
        this.customerDocumentNumber = customerDocumentNumber;
        this.amountCents = amountCents;
        this.statementDate = statementDate;
        this.rawLine = rawLine;
        this.ingestedAt = LocalDateTime.now();
        this.matched = false;
    }

    public void markMatched() {
        this.matched = true;
    }

    public Long getId() {
        return id;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public String getCustomerDocumentNumber() {
        return customerDocumentNumber;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public LocalDate getStatementDate() {
        return statementDate;
    }

    public String getRawLine() {
        return rawLine;
    }

    public LocalDateTime getIngestedAt() {
        return ingestedAt;
    }

    public boolean isMatched() {
        return matched;
    }
}
