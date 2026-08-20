package com.cardbilling.domain.repository;

import com.cardbilling.domain.Card;
import com.cardbilling.domain.CardTransaction;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardTransactionRepository extends JpaRepository<CardTransaction, Long> {

    List<CardTransaction> findByCardAndInvoiceIsNullAndTransactionDateBetween(
            Card card, LocalDateTime from, LocalDateTime to);
}
