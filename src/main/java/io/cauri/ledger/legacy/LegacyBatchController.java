package io.cauri.ledger.legacy;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Deprecado desde reporting-etl, pendiente de borrar. Nadie lo llama
 * (D7, ledger-core spec "Defectos a plantar").
 */
@RestController
@RequestMapping("/v1/legacy-batch")
public class LegacyBatchController {

    private final LegacyBatchService legacyBatchService;

    public LegacyBatchController(LegacyBatchService legacyBatchService) {
        this.legacyBatchService = legacyBatchService;
    }

    @PostMapping("/run")
    public ResponseEntity<LegacyBatchRunResponse> run() {
        LegacyBatchRun run = legacyBatchService.run();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LegacyBatchRunResponse(run.getId(), run.getRanAt(), run.getRows()));
    }

    public record LegacyBatchRunResponse(Integer id, java.time.OffsetDateTime ranAt, Integer rows) {
    }
}
