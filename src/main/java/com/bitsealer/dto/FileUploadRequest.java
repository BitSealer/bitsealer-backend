package com.bitsealer.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * Objeto que recibe el archivo subido desde el formulario.
 */
public class FileUploadRequest {

    @NotNull(message = "Debes seleccionar un archivo")
    private MultipartFile file;

    /* ────── getters y setters ────── */
    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }
}
