package com.cardbilling.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/** One execution of the reconciliation batch - the audit trail of "when did we run this, and what happened". */
@Entity
@Table(name = "reconciliation_runs")
public class ReconciliationRun {

    public enum Status {
        RUNNING, COMPLETED, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "total_lines", nullable = false)
    private int totalLines;

    @Column(name = "matched_count", nullable = false)
    private int matchedCount;

    @Column(name = "unmatched_count", nullable = false)
    private int unmatchedCount;

    @Column(name = "divergent_count", nullable = false)
    private int divergentCount;

    public ReconciliationRun() {
        this.startedAt = LocalDateTime.now();
        this.status = Status.RUNNING;
    }

    public void complete(int totalLines, int matchedCount, int unmatchedCount, int divergentCount) {
        this.totalLines = totalLines;
        this.matchedCount = matchedCount;
        this.unmatchedCount = unmatchedCount;
        this.divergentCount = divergentCount;
        this.finishedAt = LocalDateTime.now();
        this.status = Status.COMPLETED;
    }

    public void fail() {
        this.finishedAt = LocalDateTime.now();
        this.status = Status.FAILED;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public Status getStatus() {
        return status;
    }

    public int getTotalLines() {
        return totalLines;
    }

    public int getMatchedCount() {
        return matchedCount;
    }

    public int getUnmatchedCount() {
        return unmatchedCount;
    }

    public int getDivergentCount() {
        return divergentCount;
    }
}
