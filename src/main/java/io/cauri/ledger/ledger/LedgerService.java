package io.cauri.ledger.ledger;

import io.cauri.ledger.accounts.Account;
import io.cauri.ledger.accounts.AccountRepository;
import io.cauri.ledger.common.AccountNotActiveException;
import io.cauri.ledger.common.ApiException;
import io.cauri.ledger.common.InsufficientFundsException;
import io.cauri.ledger.common.NotFoundException;
import io.cauri.ledger.ledger.dto.EntryResponse;
import io.cauri.ledger.ledger.dto.TransferResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LedgerService {

    private static final String CARD_AUTH_PREFIX = "card-auth:";

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(AccountRepository accountRepository, LedgerEntryRepository ledgerEntryRepository) {
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    /**
     * POST /v1/ledger/entries. "credit" adds, "debit" subtracts. Going negative
     * is only allowed when the reference is a card authorization hold
     * (ledger-core spec: "decision histórica" for card-auth debits), otherwise
     * it is rejected as insufficient_funds.
     */
    @Transactional
    public EntryResponse createEntry(UUID accountId, BigDecimal amount, String type, String reference) {
        if (!"debit".equals(type) && !"credit".equals(type)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "validation_error", "type must be debit or credit");
        }
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new NotFoundException("account " + accountId + " not found"));

        BigDecimal delta = "credit".equals(type) ? amount : amount.negate();
        BigDecimal newBalance = account.getBalance().add(delta);

        boolean isCardAuthHold = reference != null && reference.startsWith(CARD_AUTH_PREFIX);
        if ("debit".equals(type) && newBalance.signum() < 0 && !isCardAuthHold) {
            throw new InsufficientFundsException("account " + accountId + " has insufficient funds");
        }

        account.setBalance(newBalance);
        accountRepository.save(account);

        LedgerEntry entry = new LedgerEntry(UUID.randomUUID(), accountId, amount, type, reference, OffsetDateTime.now());
        ledgerEntryRepository.save(entry);
        return EntryResponse.from(entry);
    }

    public List<EntryResponse> listByAccount(UUID accountId) {
        return ledgerEntryRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(EntryResponse::from)
                .toList();
    }

    /**
     * POST /v1/ledger/transfers. Both accounts must share the transfer's
     * currency, the source account must be active, and it must have enough
     * balance; otherwise 422 account_not_active / insufficient_funds.
     * Locks both accounts with SELECT FOR UPDATE, always in id order, so two
     * concurrent transfers between the same pair of accounts can't deadlock.
     */
    @Transactional
    public TransferResponse transfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String currency, String reference) {
        if (fromAccountId.equals(toAccountId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "validation_error", "from_account and to_account must differ");
        }

        UUID firstId = fromAccountId.compareTo(toAccountId) < 0 ? fromAccountId : toAccountId;
        UUID secondId = fromAccountId.compareTo(toAccountId) < 0 ? toAccountId : fromAccountId;

        Account first = accountRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new NotFoundException("account " + firstId + " not found"));
        Account second = accountRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new NotFoundException("account " + secondId + " not found"));

        Account from = firstId.equals(fromAccountId) ? first : second;
        Account to = firstId.equals(fromAccountId) ? second : first;

        if (!from.getCurrency().equals(currency) || !to.getCurrency().equals(currency)) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "currency_mismatch",
                    "from_account and to_account must both be in " + currency);
        }
        if (!"active".equals(from.getStatus())) {
            throw new AccountNotActiveException("account " + from.getId() + " is not active");
        }
        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("account " + from.getId() + " has insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        accountRepository.save(from);
        accountRepository.save(to);

        OffsetDateTime now = OffsetDateTime.now();
        LedgerEntry debit = new LedgerEntry(UUID.randomUUID(), from.getId(), amount, "debit", reference, now);
        LedgerEntry credit = new LedgerEntry(UUID.randomUUID(), to.getId(), amount, "credit", reference, now);
        ledgerEntryRepository.save(debit);
        ledgerEntryRepository.save(credit);

        return new TransferResponse(EntryResponse.from(debit), EntryResponse.from(credit));
    }
}
