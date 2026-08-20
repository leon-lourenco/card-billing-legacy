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

/** The outcome of matching one external statement line against internal records, within one run. */
@Entity
@Table(name = "reconciliation_matches")
public class ReconciliationMatch {

    public enum Result {
        MATCHED, DIVERGENT_AMOUNT, NOT_FOUND
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private ReconciliationRun run;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statement_line_id", nullable = false)
    private ExternalStatementLine statementLine;

    /** Null when result is NOT_FOUND. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Result result;

    @Column(name = "matched_at", nullable = false)
    private LocalDateTime matchedAt;

    protected ReconciliationMatch() {
    }

    public ReconciliationMatch(ReconciliationRun run, ExternalStatementLine statementLine, Invoice invoice, Result result) {
        this.run = run;
        this.statementLine = statementLine;
        this.invoice = invoice;
        this.result = result;
        this.matchedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public ReconciliationRun getRun() {
        return run;
    }

    public ExternalStatementLine getStatementLine() {
        return statementLine;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public Result getResult() {
        return result;
    }

    public LocalDateTime getMatchedAt() {
        return matchedAt;
    }
}
