package com.cardbilling.domain.repository;

import com.cardbilling.domain.Invoice;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByStatus(Invoice.Status status);

    /** Every invoice not yet paid and past its due date - the pool delinquency and interest-accrual both work from. */
    List<Invoice> findByStatusNotAndDueDateBefore(Invoice.Status status, LocalDate date);

    /** Every invoice not yet paid, regardless of due date - the pool reconciliation matches against. */
    List<Invoice> findByStatusNot(Invoice.Status status);
}
