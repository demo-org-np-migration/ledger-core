package io.cauri.ledger.accounts;

import io.cauri.ledger.common.NotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account create(UUID customerId, String currency) {
        Account account = new Account(UUID.randomUUID(), customerId, currency, BigDecimal.ZERO.setScale(2),
                "active", OffsetDateTime.now());
        return accountRepository.save(account);
    }

    public Account get(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("account " + id + " not found"));
    }
}
