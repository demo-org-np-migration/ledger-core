package io.cauri.ledger.accounts.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.cauri.ledger.accounts.Account;
import io.cauri.ledger.common.MoneySerializer;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID customerId,
        String currency,
        @JsonSerialize(using = MoneySerializer.class) BigDecimal balance,
        String status,
        OffsetDateTime createdAt) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getCustomerId(), account.getCurrency(),
                account.getBalance(), account.getStatus(), account.getCreatedAt());
    }
}
