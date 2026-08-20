package com.cardbilling.domain.repository;

import com.cardbilling.domain.ExternalStatementLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalStatementLineRepository extends JpaRepository<ExternalStatementLine, Long> {

    List<ExternalStatementLine> findByMatchedFalse();
}
