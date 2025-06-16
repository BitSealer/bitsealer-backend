package com.bitsealer;

import com.bitsealer.model.FileHash;
import com.bitsealer.service.FileHashService;
import com.bitsealer.user.AppUser;
import com.bitsealer.service.UserService;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
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
    public String mostrarFormulario() {
        return "upload";                      // templates/upload.html
    }

    /*────────────────────────────── 2) Procesa el archivo ───────────────────────────────*/
    @PostMapping("/upload")
    public String procesarArchivo(@RequestParam("file") MultipartFile file,
                                  RedirectAttributes redirect) {

        try {
            /* a. Usuario autenticado */
            Authentication auth   = SecurityContextHolder.getContext().getAuthentication();
            String         userId = auth.getName();                       // username (único)
            AppUser owner = userService.getByUsername(userId)             // <- helper en UserService
                                    .orElseThrow();                       // 99 % de los casos existe

            /* b. Calcular hash */
            String sha256 = DigestUtils.sha256Hex(file.getBytes());

            /* c. Guardar ligado al propietario */
            fileHashService.saveForUser(owner, sha256, file.getOriginalFilename());

            /* d. Flash para la vista */
            redirect.addFlashAttribute("hashSubido", sha256);

        } catch (IOException e) {
            redirect.addFlashAttribute("error",
                    "ERROR al procesar el archivo: " + e.getMessage());
        }

        return "redirect:/history";   // Post/Redirect/Get
    }

    /*────────────────────────────── 3) Historial personal ───────────────────────────────*/
    @GetMapping("/history")
    public String verHistorial(Model model) {

        Authentication auth   = SecurityContextHolder.getContext().getAuthentication();
        String         userId = auth.getName();
        AppUser owner = userService.getByUsername(userId).orElseThrow();

        List<FileHash> hashes = fileHashService.findAllByUser(owner);
        model.addAttribute("hashes", hashes);

        return "history";             // templates/history.html
    }
}
