package io.cauri.ledger.customers;

import io.cauri.ledger.common.ApiException;
import io.cauri.ledger.common.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CustomerService {

    // Matches the CHECK constraint on ledger.customers.kyc_status (V1__init.sql).
    private static final Set<String> VALID_KYC_STATUSES = Set.of("pending", "approved", "rejected", "manual_review");

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer create(String email, String fullName) {
        Customer customer = new Customer(UUID.randomUUID(), email, fullName, "pending", OffsetDateTime.now());
        return customerRepository.save(customer);
    }

    public Customer get(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("customer " + id + " not found"));
    }

    public Customer updateKycStatus(UUID id, String kycStatus) {
        if (!VALID_KYC_STATUSES.contains(kycStatus)) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "invalid_kyc_status",
                    "kyc_status must be one of " + VALID_KYC_STATUSES);
        }
        Customer customer = get(id);
        customer.setKycStatus(kycStatus);
        return customerRepository.save(customer);
    }

    List<Customer> listAll() {
        return customerRepository.findAll();
    }
}
