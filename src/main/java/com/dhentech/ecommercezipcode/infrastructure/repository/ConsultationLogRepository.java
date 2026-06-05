package com.dhentech.ecommercezipcode.infrastructure.repository;

import com.dhentech.ecommercezipcode.domain.ConsultationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ConsultationLogRepository extends JpaRepository<ConsultationLog, Long> {
}
