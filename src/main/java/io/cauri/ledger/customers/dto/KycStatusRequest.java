package io.cauri.ledger.customers.dto;

import jakarta.validation.constraints.NotBlank;

public record KycStatusRequest(@NotBlank String kycStatus) {
}
