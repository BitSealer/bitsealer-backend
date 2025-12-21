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

    private final FileStampRepository stampRepo;
    private final String callbackToken;

    public StampCallbackController(FileStampRepository stampRepo,
                                   @Value("${stamper.callback.token}") String callbackToken) {
        this.stampRepo = stampRepo;
        this.callbackToken = callbackToken;
    }

    @PostMapping("/callback")
    public ResponseEntity<?> callback(@RequestBody StamperCallbackRequest body,
                                     @RequestHeader(name = "X-Stamp-Token", required = false) String token) {

        if (token == null || !token.equals(callbackToken)) {
            return ResponseEntity.status(401).body("Invalid callback token");
        }

        FileStamp stamp = stampRepo.findById(body.stampId())
                .orElse(null);

        if (stamp == null) {
            return ResponseEntity.status(404).body("Stamp not found");
        }

        String status = body.status() == null ? "" : body.status().trim().toUpperCase();

        if ("SEALED".equals(status)) {
            stamp.setStatus(StampStatus.SEALED);
            stamp.setSealedAt(LocalDateTime.now());

            if (body.otsProofB64() != null && !body.otsProofB64().isBlank()) {
                stamp.setOtsProof(Base64.getDecoder().decode(body.otsProofB64()));
            }
        } else {
            stamp.setStatus(StampStatus.ERROR);
            stamp.setSealedAt(LocalDateTime.now());
        }

        stampRepo.save(stamp);
        return ResponseEntity.ok().build();
    }
}
