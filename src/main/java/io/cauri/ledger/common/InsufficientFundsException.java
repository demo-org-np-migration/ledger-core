package io.cauri.ledger.common;

import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends ApiException {
    public InsufficientFundsException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "insufficient_funds", message);
    }
}
