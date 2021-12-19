package io.cauri.ledger.customers;

import io.cauri.ledger.customers.dto.CreateCustomerRequest;
import io.cauri.ledger.customers.dto.CustomerResponse;
import io.cauri.ledger.customers.dto.KycStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = customerService.create(request.email(), request.fullName());
        return ResponseEntity.status(HttpStatus.CREATED).body(CustomerResponse.from(customer));
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable UUID id) {
        return CustomerResponse.from(customerService.get(id));
    }

    @PatchMapping("/{id}/kyc-status")
    public CustomerResponse updateKycStatus(@PathVariable UUID id, @Valid @RequestBody KycStatusRequest request) {
        return CustomerResponse.from(customerService.updateKycStatus(id, request.kycStatus()));
    }
}
