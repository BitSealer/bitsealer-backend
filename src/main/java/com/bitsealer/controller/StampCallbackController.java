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

        String status = body.status() == null ? "" : body.status().trim().toUpperCase();

        // Guardar proof si viene (PENDING o SEALED)
        if (body.otsProofB64() != null && !body.otsProofB64().isBlank()) {
            stamp.setOtsProof(Base64.getDecoder().decode(body.otsProofB64()));
        }

        switch (status) {
            case "PENDING" -> {
                stamp.setStatus(StampStatus.PENDING);
                stamp.setSealedAt(null);
            }
            case "SEALED" -> {
                stamp.setStatus(StampStatus.SEALED);
                stamp.setSealedAt(LocalDateTime.now());
            }
            default -> {
                stamp.setStatus(StampStatus.ERROR);
                stamp.setSealedAt(LocalDateTime.now());
            }
        }

        fileStampRepository.save(stamp);
        return ResponseEntity.ok().build();
    }
}
