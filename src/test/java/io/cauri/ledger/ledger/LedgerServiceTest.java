package io.cauri.ledger.ledger;

import io.cauri.ledger.accounts.Account;
import io.cauri.ledger.accounts.AccountRepository;
import io.cauri.ledger.common.AccountNotActiveException;
import io.cauri.ledger.common.InsufficientFundsException;
import io.cauri.ledger.customers.Customer;
import io.cauri.ledger.customers.CustomerRepository;
import io.cauri.ledger.ledger.dto.TransferResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ledger-core spec, "Tests mínimos": transferencia feliz, fondos
 * insuficientes, cuenta congelada.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LedgerServiceTest {

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Account from;
    private Account to;

    @BeforeEach
    void setUp() {
        Customer customer = customerRepository.save(new Customer(UUID.randomUUID(), "transfer-test-" + UUID.randomUUID() + "@cauri.io",
                "Cliente de Test", "approved", OffsetDateTime.now()));
        UUID customerId = customer.getId();
        from = accountRepository.save(new Account(UUID.randomUUID(), customerId, "ARS",
                new BigDecimal("1000.00"), "active", OffsetDateTime.now()));
        to = accountRepository.save(new Account(UUID.randomUUID(), customerId, "ARS",
                new BigDecimal("0.00"), "active", OffsetDateTime.now()));
    }

    @Test
    void happyTransferMovesBalanceAndWritesBothEntries() {
        TransferResponse response = ledgerService.transfer(from.getId(), to.getId(), new BigDecimal("300.00"), "ARS", "test-transfer");

        assertThat(response.debitEntry().type()).isEqualTo("debit");
        assertThat(response.creditEntry().type()).isEqualTo("credit");

        Account reloadedFrom = accountRepository.findById(from.getId()).orElseThrow();
        Account reloadedTo = accountRepository.findById(to.getId()).orElseThrow();
        assertThat(reloadedFrom.getBalance()).isEqualByComparingTo("700.00");
        assertThat(reloadedTo.getBalance()).isEqualByComparingTo("300.00");
    }

    @Test
    void insufficientFundsIsRejected() {
        assertThatThrownBy(() -> ledgerService.transfer(from.getId(), to.getId(), new BigDecimal("5000.00"), "ARS", "too-much"))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void frozenSourceAccountIsRejected() {
        from.setStatus("frozen");
        accountRepository.save(from);

        assertThatThrownBy(() -> ledgerService.transfer(from.getId(), to.getId(), new BigDecimal("10.00"), "ARS", "blocked"))
                .isInstanceOf(AccountNotActiveException.class);
    }
}
