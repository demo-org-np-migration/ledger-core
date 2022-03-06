package io.cauri.ledger.ledger.dto;

public record TransferResponse(EntryResponse debitEntry, EntryResponse creditEntry) {
}
