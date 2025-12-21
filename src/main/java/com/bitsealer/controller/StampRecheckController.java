package com.bitsealer.controller;

import com.bitsealer.service.StampRecheckService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stamps")
public class StampRecheckController {

    private final StampRecheckService stampRecheckService;

    public StampRecheckController(StampRecheckService stampRecheckService) {
        this.stampRecheckService = stampRecheckService;
    }

    @PostMapping("/recheck")
    public ResponseEntity<?> recheck(@RequestParam(defaultValue = "50") int limit) {
        int processed = stampRecheckService.recheckDue(Math.max(1, limit));
        return ResponseEntity.ok(Map.of("processed", processed));
    }
}
