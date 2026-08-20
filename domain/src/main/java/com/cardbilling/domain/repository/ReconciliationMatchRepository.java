package com.cardbilling.domain.repository;

import com.cardbilling.domain.ReconciliationMatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReconciliationMatchRepository extends JpaRepository<ReconciliationMatch, Long> {
}
