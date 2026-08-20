package com.cardbilling.domain.repository;

import com.cardbilling.domain.Card;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByStatus(Card.Status status);
}
