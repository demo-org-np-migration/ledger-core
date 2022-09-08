package io.cauri.ledger.legacy;

import io.cauri.ledger.ledger.LedgerEntryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Dead module (D7): used to run the daily ledger close before reporting-etl
 * existed. Left in place, still wired, still migrated. Nobody calls it.
 */
@Service
public class LegacyBatchService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final LegacyBatchRunRepository legacyBatchRunRepository;

    public LegacyBatchService(LedgerEntryRepository ledgerEntryRepository, LegacyBatchRunRepository legacyBatchRunRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.legacyBatchRunRepository = legacyBatchRunRepository;
    }

    public LegacyBatchRun run() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        long entriesToday = ledgerEntryRepository.findAll().stream()
                .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().toLocalDate().equals(today))
                .count();
        LegacyBatchRun batchRun = new LegacyBatchRun(OffsetDateTime.now(), (int) entriesToday);
        return legacyBatchRunRepository.save(batchRun);
    }
}
