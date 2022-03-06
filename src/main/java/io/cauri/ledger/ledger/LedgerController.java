package io.cauri.ledger.ledger;

import io.cauri.ledger.ledger.dto.CreateEntryRequest;
import io.cauri.ledger.ledger.dto.EntryResponse;
import io.cauri.ledger.ledger.dto.TransferRequest;
import io.cauri.ledger.ledger.dto.TransferResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/ledger")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping("/entries")
    public ResponseEntity<EntryResponse> createEntry(@Valid @RequestBody CreateEntryRequest request) {
        EntryResponse entry = ledgerService.createEntry(request.accountId(), request.amount(), request.type(), request.reference());
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    @PostMapping("/transfers")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransferResponse response = ledgerService.transfer(request.fromAccount(), request.toAccount(),
                request.amount(), request.currency(), request.reference());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/entries")
    public List<EntryResponse> listEntries(@RequestParam("account_id") UUID accountId) {
        return ledgerService.listByAccount(accountId);
    }
}
