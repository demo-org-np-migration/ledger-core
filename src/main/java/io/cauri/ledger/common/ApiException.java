package io.cauri.ledger.common;

import org.springframework.http.HttpStatus;

/**
 * Base exception for domain errors that must be surfaced to callers as the
 * standard {"error": {"code", "message"}} envelope (las convenciones internas de API).
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
