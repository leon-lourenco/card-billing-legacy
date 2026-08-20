package com.cardbilling.scheduling;

import com.cardbilling.delinquency.DelinquencyJob;
import com.cardbilling.interest.InterestAccrualJob;
import com.cardbilling.invoiceclosing.InvoiceClosingJob;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The nightly batch chain, run in a fixed sequence once a day - the same three jobs the
 * {@code /jobs/*} endpoints trigger manually, just on a cron instead of on demand. Reconciliation
 * isn't in this chain: it depends on a statement file actually having been ingested first, so it
 * stays a deliberate, API-triggered step rather than something that fires on a timer.
 */
@Component
public class DailyBatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyBatchScheduler.class);

    private final InvoiceClosingJob invoiceClosingJob;
    private final DelinquencyJob delinquencyJob;
    private final InterestAccrualJob interestAccrualJob;

    public DailyBatchScheduler(InvoiceClosingJob invoiceClosingJob, DelinquencyJob delinquencyJob,
            InterestAccrualJob interestAccrualJob) {
        this.invoiceClosingJob = invoiceClosingJob;
        this.delinquencyJob = delinquencyJob;
        this.interestAccrualJob = interestAccrualJob;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void runNightlyChain() {
        LocalDate today = LocalDate.now();
        int closed = invoiceClosingJob.closeCyclesFor(today);
        int notified = delinquencyJob.checkFor(today);
        int accrued = interestAccrualJob.accrueFor(today);
        log.info("Nightly batch chain complete: {} invoices closed, {} notified, {} accrued interest",
                closed, notified, accrued);
    }
}
