package io.cauri.ledger.common;

/**
 * Error envelope shape mandated by las convenciones internas de API:
 * {"error": {"code": "<snake_case>", "message": "<texto>"}}
 */
public record ApiError(ErrorBody error) {

    public static ApiError of(String code, String message) {
        return new ApiError(new ErrorBody(code, message));
    }

    public record ErrorBody(String code, String message) {
    }
}
