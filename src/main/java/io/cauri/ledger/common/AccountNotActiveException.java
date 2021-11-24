package io.cauri.ledger.common;

import org.springframework.http.HttpStatus;

public class AccountNotActiveException extends ApiException {
    public AccountNotActiveException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "account_not_active", message);
    }
}
