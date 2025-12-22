package com.bitsealer.controller;

import com.bitsealer.dto.StamperCallbackRequest;
import com.bitsealer.model.FileStamp;
import com.bitsealer.model.StampStatus;
import com.bitsealer.repository.FileStampRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Base64;

@RestController
@RequestMapping("/api/stamps")
public class StampCallbackController {

    private final FileStampRepository fileStampRepository;
    private final String callbackToken;

    public StampCallbackController(FileStampRepository fileStampRepository,
                                   @Value("${stamper.callback.token:}") String callbackToken) {
        this.fileStampRepository = fileStampRepository;
        this.callbackToken = callbackToken;
    }

    @PostMapping("/callback")
    public ResponseEntity<?> callback(
            @RequestHeader(value = "X-Stamp-Token", required = false) String token,
            @RequestBody StamperCallbackRequest body
    ) {
        if (callbackToken != null && !callbackToken.isBlank()) {
            if (token == null || !callbackToken.equals(token)) {
                return ResponseEntity.status(401).build();
            }
        }

        FileStamp stamp = fileStampRepository.findById(body.stampId())
                .orElseThrow(() -> new RuntimeException("Stamp no encontrado: " + body.stampId()));

        String status = (body.status() == null) ? "" : body.status().trim().toUpperCase();

        // proof (si llega)
        if (body.otsProofB64() != null && !body.otsProofB64().isBlank()) {
            stamp.setOtsProof(Base64.getDecoder().decode(body.otsProofB64()));
        }

        // txid (si llega y es válido) -> NO lo pises con vacío
        if (body.txid() != null) {
            String txid = body.txid().trim();
            if (txid.matches("^[0-9a-fA-F]{64}$")) {
                stamp.setTxid(txid.toLowerCase());
            }
        }

        // error msg (si llega)
        if (body.errorMessage() != null && !body.errorMessage().isBlank()) {
            stamp.setLastError(body.errorMessage().trim());
        } else {
            stamp.setLastError(null);
        }

        // Estado
        switch (status) {
            case "PENDING" -> {
                stamp.setStatus(StampStatus.PENDING);
                stamp.setSealedAt(null);
            }
            case "ANCHORING" -> {
                stamp.setStatus(StampStatus.ANCHORING);
                stamp.setSealedAt(null);
            }
            case "SEALED" -> {
                stamp.setStatus(StampStatus.SEALED);
                if (stamp.getSealedAt() == null) stamp.setSealedAt(LocalDateTime.now());
                stamp.setNextCheckAt(null);
                stamp.setLastError(null);
            }
            case "ERROR" -> {
                // ERROR no es "sealed"
                stamp.setStatus(StampStatus.ERROR);
                stamp.setSealedAt(null);
                stamp.setNextCheckAt(null);
            }
            default -> {
                // si llega algo raro, no mates el flujo: vuelve a PENDING/ANCHORING según txid
                stamp.setStatus((stamp.getTxid() != null && !stamp.getTxid().isBlank())
                        ? StampStatus.ANCHORING
                        : StampStatus.PENDING);
                stamp.setSealedAt(null);
            }
        }

        stamp.setLastCheckedAt(LocalDateTime.now());

        fileStampRepository.save(stamp);
        return ResponseEntity.ok().build();
    }
}
