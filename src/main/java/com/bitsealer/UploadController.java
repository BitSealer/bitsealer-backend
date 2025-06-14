package com.bitsealer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UploadController {

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> handleFileUpload(@RequestParam("file") MultipartFile file)
            throws IOException, NoSuchAlgorithmException {

        // Leer el archivo en bytes
        byte[] fileBytes = file.getBytes();

        // Calcular el hash SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(fileBytes);

        // Convertir el resultado a texto hexadecimal
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }

        // Devolver el hash como respuesta JSON
        Map<String, String> response = new HashMap<>();
        response.put("sha256", hexString.toString());
        return ResponseEntity.ok(response);
    }
}
