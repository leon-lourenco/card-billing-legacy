package com.cardbilling.domain.repository;

import com.cardbilling.domain.Invoice;
import com.cardbilling.domain.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoice(Invoice invoice);
}
