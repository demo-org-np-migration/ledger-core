package io.cauri.ledger.ledger.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.cauri.ledger.common.MoneySerializer;
import io.cauri.ledger.ledger.LedgerEntry;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record EntryResponse(
        UUID id,
        UUID accountId,
        @JsonSerialize(using = MoneySerializer.class) BigDecimal amount,
        String type,
        String reference,
        OffsetDateTime createdAt) {

    public static EntryResponse from(LedgerEntry entry) {
        return new EntryResponse(entry.getId(), entry.getAccountId(), entry.getAmount(), entry.getType(),
                entry.getReference(), entry.getCreatedAt());
    }
}
