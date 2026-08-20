package com.cardbilling.web;

import com.cardbilling.delinquency.DelinquencyJob;
import com.cardbilling.domain.ReconciliationRun;
import com.cardbilling.interest.InterestAccrualJob;
import com.cardbilling.invoiceclosing.InvoiceClosingJob;
import com.cardbilling.reconciliation.ReconciliationMatchJob;
import com.cardbilling.reconciliation.StatementIngestJob;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Manual triggers for every batch job - in a real legacy system these would only ever fire off
 * a cron entry. Exposing them here too doesn't change what the jobs do; it just means a demo
 * doesn't have to wait for midnight or fake the system clock to show one running.
 */
@RestController
@RequestMapping("/jobs")
@Tag(name = "Jobs", description = "Manual triggers for each batch job, alongside their own daily @Scheduled run")
public class JobController {

    private final InvoiceClosingJob invoiceClosingJob;
    private final DelinquencyJob delinquencyJob;
    private final InterestAccrualJob interestAccrualJob;
    private final StatementIngestJob statementIngestJob;
    private final ReconciliationMatchJob reconciliationMatchJob;

    public JobController(InvoiceClosingJob invoiceClosingJob, DelinquencyJob delinquencyJob,
            InterestAccrualJob interestAccrualJob, StatementIngestJob statementIngestJob,
            ReconciliationMatchJob reconciliationMatchJob) {
        this.invoiceClosingJob = invoiceClosingJob;
        this.delinquencyJob = delinquencyJob;
        this.interestAccrualJob = interestAccrualJob;
        this.statementIngestJob = statementIngestJob;
        this.reconciliationMatchJob = reconciliationMatchJob;
    }

    @PostMapping("/invoice-closing/run")
    @Operation(summary = "Close every active card's billing cycle whose cycle day matches the given date")
    public Map<String, Object> runInvoiceClosing(@RequestParam(required = false) String date) {
        int closed = invoiceClosingJob.closeCyclesFor(resolveDate(date));
        return Map.of("invoicesClosed", closed);
    }

    @PostMapping("/delinquency/run")
    @Operation(summary = "Detect overdue invoices and escalate notification (D+5/D+15/D+30)")
    public Map<String, Object> runDelinquency(@RequestParam(required = false) String date) {
        int notified = delinquencyJob.checkFor(resolveDate(date));
        return Map.of("invoicesNotified", notified);
    }

    @PostMapping("/interest-accrual/run")
    @Operation(summary = "Apply the late fee and daily interest to every invoice overdue today")
    public Map<String, Object> runInterestAccrual(@RequestParam(required = false) String date) {
        int accrued = interestAccrualJob.accrueFor(resolveDate(date));
        return Map.of("invoicesAccrued", accrued);
    }

    @PostMapping("/reconciliation/ingest")
    @Operation(summary = "Ingest an external statement CSV (external_reference,document_number,amount_cents,statement_date)")
    public ResponseEntity<Map<String, Object>> ingestStatement(@RequestParam("file") MultipartFile file) throws IOException {
        Path tempFile = Files.createTempFile("statement-", ".csv");
        file.transferTo(tempFile);
        try {
            int ingested = statementIngestJob.ingest(tempFile);
            return ResponseEntity.ok(Map.of("linesIngested", ingested));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @PostMapping("/reconciliation/match")
    @Operation(summary = "Match every unmatched statement line against open invoices")
    public Map<String, Object> runReconciliationMatch() {
        ReconciliationRun run = reconciliationMatchJob.run();
        return Map.of(
                "runId", run.getId(),
                "totalLines", run.getTotalLines(),
                "matched", run.getMatchedCount(),
                "divergent", run.getDivergentCount(),
                "notFound", run.getUnmatchedCount());
    }

    private LocalDate resolveDate(String date) {
        return date == null ? LocalDate.now() : LocalDate.parse(date);
    }
}
