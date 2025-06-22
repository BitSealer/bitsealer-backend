package com.bitsealer.controller;

import com.bitsealer.dto.FileUploadRequest;          // ‖ nuevo
import com.bitsealer.model.FileHash;               // (si ya cambiaste a entity)
import com.bitsealer.model.AppUser;                // idem
import com.bitsealer.service.FileHashService;
import com.bitsealer.service.UserService;

import jakarta.validation.Valid;                     // ‖ nuevo
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;   // ‖ nuevo
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute; // ‖ nuevo
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class FileUploadController {

    private final FileHashService fileHashService;
    private final UserService     userService;

    public FileUploadController(FileHashService fileHashService,
                                UserService userService) {
        this.fileHashService = fileHashService;
        this.userService     = userService;
    }

    /*────────────────────────────── 1) Formulario de subida ──────────────────────────────*/
    @GetMapping("/upload")
    public String mostrarFormulario(Model model) {
        model.addAttribute("fileUploadRequest", new FileUploadRequest());   // ‖ para <form th:object>
        return "upload";                       // templates/upload.html
    }

    /*────────────────────────────── 2) Procesa el archivo ───────────────────────────────*/
    @PostMapping("/upload")
    public String procesarArchivo(@Valid
                                  @ModelAttribute("fileUploadRequest") FileUploadRequest req, // ‖
                                  BindingResult br,                                           // ‖
                                  RedirectAttributes redirect) {

        // a) Validación
        if (br.hasErrors() || req.getFile().isEmpty()) {
            redirect.addFlashAttribute("error", "Debes seleccionar un archivo");
            return "redirect:/upload";
        }

        // b) Usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username     = auth.getName();
        AppUser owner = userService.getByUsername(username).orElseThrow();

        // c) Guardar (la lógica de hash va en el servicio)
        try {
            fileHashService.saveForUser(owner, req.getFile());              // ‖ adaptado
            redirect.addFlashAttribute("success", "Archivo sellado correctamente");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "ERROR al procesar el archivo: " + e.getMessage());
        }

        return "redirect:/history";   // Post/Redirect/Get
    }

    /*──────────────────────── 3) Historial personal ───────────────────────*/
    @GetMapping("/history")
    public String verHistorial(Model model) {

        String username = SecurityContextHolder.getContext()
                                            .getAuthentication()
                                            .getName();
        AppUser owner = userService.getByUsername(username).orElseThrow();

        // ahora obtenemos DTOs
        model.addAttribute("hashes",
                fileHashService.findDtosByUser(owner));

        return "history";   // templates/history.html
    }
}
