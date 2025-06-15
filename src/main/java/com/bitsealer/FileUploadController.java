package com.bitsealer;

import com.bitsealer.service.FileHashService;
import com.bitsealer.model.FileHash;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class FileUploadController {

    private final FileHashService fileHashService;

    // ──────────────────────────────────────────────────────────────────────────────
    // INYECCIÓN (Vía constructor) – Spring se encarga de pasar la instancia
    // ──────────────────────────────────────────────────────────────────────────────
    public FileUploadController(FileHashService fileHashService) {
        this.fileHashService = fileHashService;
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // FORMULARIO DE SUBIDA (GET)
    // ──────────────────────────────────────────────────────────────────────────────
    @GetMapping("/upload")
    public String mostrarFormulario() {
        return "upload";          // templates/upload.html
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // PROCESA EL ARCHIVO (POST)  ➜ PRG → redirige a /history
    // ──────────────────────────────────────────────────────────────────────────────
    @PostMapping("/upload")
    public String procesarArchivo(@RequestParam("file") MultipartFile file,
                                  RedirectAttributes redirect) {

        try {
            // 1. Calcular SHA-256 (Apache Commons Codec)
            String sha256 = DigestUtils.sha256Hex(file.getBytes());

            // 2. Persistir en base de datos
            fileHashService.save(sha256, file.getOriginalFilename());

            // 3. Flash attribute para mostrar aviso en /history
            redirect.addFlashAttribute("hashSubido", sha256);
        } catch (IOException e) {
            redirect.addFlashAttribute("error", "ERROR al procesar el archivo: " + e.getMessage());
        }

        // Patrón Post/Redirect/Get
        return "redirect:/history";
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // HISTORIAL DE SELLADOS (GET)
    // ──────────────────────────────────────────────────────────────────────────────
    @GetMapping("/history")
    public String verHistorial(Model model) {
        List<FileHash> hashes = fileHashService.findAll();
        model.addAttribute("hashes", hashes);           // lista para la tabla
        return "history";                               // templates/history.html
    }
}
