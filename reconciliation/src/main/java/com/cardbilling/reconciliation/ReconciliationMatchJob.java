package com.cardbilling.reconciliation;

import com.cardbilling.domain.Customer;
import com.cardbilling.domain.ExternalStatementLine;
import com.cardbilling.domain.Invoice;
import com.cardbilling.domain.Payment;
import com.cardbilling.domain.ReconciliationMatch;
import com.cardbilling.domain.ReconciliationRun;
import com.cardbilling.domain.repository.ExternalStatementLineRepository;
import com.cardbilling.domain.repository.InvoiceRepository;
import com.cardbilling.domain.repository.PaymentRepository;
import com.cardbilling.domain.repository.ReconciliationMatchRepository;
import com.cardbilling.domain.repository.ReconciliationRunRepository;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Matches every unmatched external statement line against every unpaid invoice, one nested loop
 * at a time - O(lines x invoices). There is no shared identifier between the two sides (an
 * external bank statement and this system's own invoices were never going to agree on an id),
 * so a match is decided by customer document number, amount, and a statement date within
 * {@value #DATE_TOLERANCE_DAYS} days of the invoice's due date. That's the realistic case where
 * this exact anti-pattern shows up: nothing here could be answered with a single indexed
 * lookup, so it gets answered by scanning.
 */
@Component
public class ReconciliationMatchJob {

    private static final int DATE_TOLERANCE_DAYS = 3;

    private final ExternalStatementLineRepository statementLineRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ReconciliationRunRepository runRepository;
    private final ReconciliationMatchRepository matchRepository;

    public ReconciliationMatchJob(ExternalStatementLineRepository statementLineRepository,
            InvoiceRepository invoiceRepository, PaymentRepository paymentRepository,
            ReconciliationRunRepository runRepository, ReconciliationMatchRepository matchRepository) {
        this.statementLineRepository = statementLineRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.runRepository = runRepository;
        this.matchRepository = matchRepository;
    }

    @Transactional
    public ReconciliationRun run() {
        ReconciliationRun run = new ReconciliationRun();
        runRepository.save(run);

        List<ExternalStatementLine> lines = statementLineRepository.findByMatchedFalse();
        List<Invoice> openInvoices = invoiceRepository.findByStatusNot(Invoice.Status.PAID);

        int matched = 0;
        int divergent = 0;
        int notFound = 0;

        for (ExternalStatementLine line : lines) {
            Invoice match = null;
            boolean sawDivergentAmount = false;

            for (Invoice invoice : openInvoices) {
                if (!candidateMatches(invoice, line)) {
                    continue;
                }
                long invoiceOwedCents = invoice.getTotalAmountCents() + invoice.getInterestAppliedCents();
                if (invoiceOwedCents == line.getAmountCents()) {
                    match = invoice;
                    break;
                }
                sawDivergentAmount = true;
            }

            ReconciliationMatch.Result result = recordOutcome(run, line, match, sawDivergentAmount);
            switch (result) {
                case MATCHED:
                    matched++;
                    break;
                case DIVERGENT_AMOUNT:
                    divergent++;
                    break;
                default:
                    notFound++;
            }
        }

        run.complete(lines.size(), matched, notFound, divergent);
        runRepository.save(run);
        return run;
    }

    private boolean candidateMatches(Invoice invoice, ExternalStatementLine line) {
        Customer customer = invoice.getCard().getAccount().getCustomer();
        if (!customer.getDocumentNumber().equals(line.getCustomerDocumentNumber())) {
            return false;
        }
        long daysBetween = Math.abs(ChronoUnit.DAYS.between(invoice.getDueDate(), line.getStatementDate()));
        return daysBetween <= DATE_TOLERANCE_DAYS;
    }

    private ReconciliationMatch.Result recordOutcome(ReconciliationRun run, ExternalStatementLine line,
            Invoice match, boolean sawDivergentAmount) {
        ReconciliationMatch.Result result;
        if (match != null) {
            result = ReconciliationMatch.Result.MATCHED;
            line.markMatched();
            statementLineRepository.save(line);
            paymentRepository.save(new Payment(match, line.getAmountCents(), line.getStatementDate().atStartOfDay(),
                    Payment.Source.EXTERNAL_RECONCILIATION, line.getExternalReference()));
            match.markPaid();
            invoiceRepository.save(match);
        } else if (sawDivergentAmount) {
            result = ReconciliationMatch.Result.DIVERGENT_AMOUNT;
        } else {
            result = ReconciliationMatch.Result.NOT_FOUND;
        }
        matchRepository.save(new ReconciliationMatch(run, line, match, result));
        return result;
    }
}
