package com.bitsealer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;

@Controller
public class FileUploadController {

    @GetMapping("/upload")
    public String mostrarFormulario() {
        return "upload"; // Renderiza upload.html
    }

    @PostMapping("/upload")
    public String procesarArchivo(@RequestParam("file") MultipartFile file, Model model) {
        try {
            byte[] contenido = file.getBytes();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(contenido);

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }

            model.addAttribute("hash", sb.toString());
        } catch (IOException | java.security.NoSuchAlgorithmException e) {
            model.addAttribute("hash", "ERROR: " + e.getMessage());
        }

        return "upload"; // Vuelve a mostrar la página con el resultado
    }
}
