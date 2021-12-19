package io.cauri.ledger.customers.dto;

import io.cauri.ledger.customers.Customer;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CustomerResponse(UUID id, String email, String fullName, String kycStatus, OffsetDateTime createdAt) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getEmail(), customer.getFullName(),
                customer.getKycStatus(), customer.getCreatedAt());
    }
}
