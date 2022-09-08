package io.cauri.ledger.legacy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Dead module: the daily closing batch this table backed was superseded by
 * reporting-etl. Nothing in the org calls POST /v1/legacy-batch/run anymore
 * (D7, ledger-core spec "Defectos a plantar").
 */
@Entity
@Table(name = "legacy_batch_runs")
public class LegacyBatchRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ran_at", nullable = false)
    private OffsetDateTime ranAt;

    @Column(nullable = false)
    private Integer rows;

    protected LegacyBatchRun() {
        // JPA
    }

    public LegacyBatchRun(OffsetDateTime ranAt, Integer rows) {
        this.ranAt = ranAt;
        this.rows = rows;
    }

    public Integer getId() {
        return id;
    }

    public OffsetDateTime getRanAt() {
        return ranAt;
    }

    public Integer getRows() {
        return rows;
    }
}
