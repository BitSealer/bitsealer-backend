package com.bitsealer.controller;

import com.bitsealer.dto.DashboardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DashboardController {

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboardPlaceholder() {
        DashboardResponse response = new DashboardResponse(); // todo vacío o nulo
        return ResponseEntity.ok(response);
    }
}
