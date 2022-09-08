package io.cauri.ledger.legacy;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LegacyBatchRunRepository extends JpaRepository<LegacyBatchRun, Integer> {
}
