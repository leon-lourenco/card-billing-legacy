package com.cardbilling.interest;

import com.cardbilling.domain.Invoice;
import com.cardbilling.domain.repository.InvoiceRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applies a flat 2% late fee (once, the first time an invoice goes overdue) plus 1% simple
 * daily interest on every invoice still open past its due date - the same rule a Brazilian
 * card issuer's "multa + mora diária" typically follows. Interest accrues on the invoice's
 * original total, not on interest already applied - simple, not compounding.
 *
 * <p>Idempotency guard: {@code lastInterestAccrualDate} tracks the last day this invoice was
 * touched. Running the job twice on the same day is a no-op for every invoice already accrued
 * that day, so a rerun after a partial failure doesn't double-charge.
 */
@Component
public class InterestAccrualJob {

    private static final double LATE_FEE_RATE = 0.02;
    private static final double DAILY_INTEREST_RATE = 0.01;

    private final InvoiceRepository invoiceRepository;

    public InterestAccrualJob(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public int accrueFor(LocalDate today) {
        List<Invoice> overdue = invoiceRepository.findByStatusNotAndDueDateBefore(Invoice.Status.PAID, today);

        int accruedCount = 0;
        for (Invoice invoice : overdue) {
            if (today.equals(invoice.getLastInterestAccrualDate())) {
                continue;
            }
            invoice.markOverdue();
            invoice.applyInterest(amountToAccrue(invoice), today);
            invoiceRepository.save(invoice);
            accruedCount++;
        }
        return accruedCount;
    }

    private long amountToAccrue(Invoice invoice) {
        boolean firstAccrual = invoice.getLastInterestAccrualDate() == null;
        long lateFeeCents = firstAccrual ? Math.round(invoice.getTotalAmountCents() * LATE_FEE_RATE) : 0;
        long dailyInterestCents = Math.round(invoice.getTotalAmountCents() * DAILY_INTEREST_RATE);
        return lateFeeCents + dailyInterestCents;
    }
}
