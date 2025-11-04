package com.bitsealer.controller;

import com.bitsealer.dto.FileHashDto;
import com.bitsealer.service.FileHashService;
import com.bitsealer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileHashService fileHashService;
    private final UserService userService;

    @Autowired
    public FileUploadController(FileHashService fileHashService, UserService userService) {
        this.fileHashService = fileHashService;
        this.userService     = userService;
    }

    /**
     * Endpoint para subir un archivo y calcular/guardar su hash.
     * Requiere autenticación (el usuario se infiere del token).
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        Authentication authentication) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Debes seleccionar un archivo");
        }
        try {
            // Obtener usuario actual (del token)
            String username = authentication.getName();
            // (Opcional: cargar entidad AppUser si hace falta)
            // Guardar archivo y obtener DTO
            FileHashDto savedFileDto = fileHashService.saveForCurrentUser(file);
            return ResponseEntity.status(201).body(savedFileDto);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error al leer el archivo");
        }
    }

    /**
     * Endpoint para obtener el historial de archivos del usuario actual.
     * Requiere autenticación.
     */
    @GetMapping("/history")
    public ResponseEntity<List<FileHashDto>> getHistory(Authentication authentication) {
        // Authentication trae el principal, podemos usarlo:
        List<FileHashDto> history = fileHashService.listMineDtos();
        return ResponseEntity.ok(history);
    }
}
