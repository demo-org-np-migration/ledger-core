package io.cauri.ledger.accounts.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.cauri.ledger.accounts.Account;
import io.cauri.ledger.common.MoneySerializer;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceResponse(
        UUID accountId,
        @JsonSerialize(using = MoneySerializer.class) BigDecimal balance,
        String currency) {

    public static BalanceResponse from(Account account) {
        return new BalanceResponse(account.getId(), account.getBalance(), account.getCurrency());
    }
}
