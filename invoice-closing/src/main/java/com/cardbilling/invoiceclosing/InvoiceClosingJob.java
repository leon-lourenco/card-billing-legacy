package com.cardbilling.invoiceclosing;

import com.cardbilling.domain.Card;
import com.cardbilling.domain.CardTransaction;
import com.cardbilling.domain.Invoice;
import com.cardbilling.domain.repository.CardRepository;
import com.cardbilling.domain.repository.CardTransactionRepository;
import com.cardbilling.domain.repository.InvoiceRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Closes a billing cycle for every active card whose cycle day matches the given date: gathers
 * every transaction not yet on an invoice, sums them into a new Invoice, and links each
 * transaction to it. Ten days between closing and due date, matching typical card billing.
 */
@Component
public class InvoiceClosingJob {

    private static final DateTimeFormatter REFERENCE_MONTH = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final int DAYS_UNTIL_DUE = 10;

    private final CardRepository cardRepository;
    private final CardTransactionRepository cardTransactionRepository;
    private final InvoiceRepository invoiceRepository;

    public InvoiceClosingJob(CardRepository cardRepository, CardTransactionRepository cardTransactionRepository,
            InvoiceRepository invoiceRepository) {
        this.cardRepository = cardRepository;
        this.cardTransactionRepository = cardTransactionRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public int closeCyclesFor(LocalDate closingDate) {
        List<Card> activeCards = cardRepository.findByStatus(Card.Status.ACTIVE);

        int closedCount = 0;
        for (Card card : activeCards) {
            if (card.getBillingCycleDay() != closingDate.getDayOfMonth()) {
                continue;
            }
            if (closeCycle(card, closingDate)) {
                closedCount++;
            }
        }
        return closedCount;
    }

    private boolean closeCycle(Card card, LocalDate closingDate) {
        LocalDateTime cycleStart = closingDate.minusMonths(1).atStartOfDay();
        LocalDateTime cycleEnd = closingDate.atStartOfDay();

        List<CardTransaction> transactions = cardTransactionRepository
                .findByCardAndInvoiceIsNullAndTransactionDateBetween(card, cycleStart, cycleEnd);
        if (transactions.isEmpty()) {
            return false;
        }

        long totalCents = 0;
        for (CardTransaction transaction : transactions) {
            totalCents += transaction.getAmountCents();
        }

        Invoice invoice = new Invoice(card, closingDate.format(REFERENCE_MONTH), closingDate,
                closingDate.plusDays(DAYS_UNTIL_DUE), totalCents);
        invoiceRepository.save(invoice);

        for (CardTransaction transaction : transactions) {
            transaction.assignToInvoice(invoice);
            cardTransactionRepository.save(transaction);
        }
        return true;
    }
}
