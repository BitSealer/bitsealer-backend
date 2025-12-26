package com.bitsealer.controller;

import com.bitsealer.service.FileArtifactService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class FileArtifactController {

    private final FileArtifactService fileArtifactService;

    public FileArtifactController(FileArtifactService fileArtifactService) {
        this.fileArtifactService = fileArtifactService;
    }

    @GetMapping("/{fileHashId}/ots")
    public ResponseEntity<byte[]> downloadOts(@PathVariable Long fileHashId, Authentication authentication) {
        var res = fileArtifactService.downloadOts(fileHashId, authentication.getName());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, res.contentType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + res.filename() + "\"")
                .body(res.bytes());
    }

    @GetMapping("/{fileHashId}/certificate")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long fileHashId, Authentication authentication) {
        var res = fileArtifactService.downloadCertificatePdf(fileHashId, authentication.getName());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, res.contentType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + res.filename() + "\"")
                .body(res.bytes());
    }
}
