package com.opsbridge.incident;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DiagnosticCheckRepository extends JpaRepository<DiagnosticCheck, Long> {
    Optional<DiagnosticCheck> findByIdAndIncidentId(Long id, Long incidentId);
}
