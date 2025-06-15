package com.bitsealer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String showUploadForm() {
        // Este nombre debe coincidir con el archivo HTML: upload.html
        return "upload";
    }
}
