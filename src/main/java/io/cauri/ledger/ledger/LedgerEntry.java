package io.cauri.ledger.ledger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String type;

    @Column
    private String reference;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected LedgerEntry() {
        // JPA
    }

    public LedgerEntry(UUID id, UUID accountId, BigDecimal amount, String type, String reference, OffsetDateTime createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
        this.reference = reference;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getReference() {
        return reference;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
