package dev.siample.cv.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/cv")
public class CvController {

    @GetMapping({ "", "/" })
    public ResponseEntity<Resource> downloadDefaultCv() throws IOException {
        return downloadCv("en");
    }

    @GetMapping("/{lang}")
    public ResponseEntity<Resource> downloadCv(@PathVariable String lang) throws IOException {

        String fileName = switch (lang.toLowerCase()) {
            case "de" -> "VelemiAgnesCV_2026_Jan_DE.pdf";
            case "en" -> "VelemiAgnesCV_2026_Jan_EN.pdf";
            default -> throw new IllegalArgumentException("Unsupported language");
        };

        ClassPathResource file = new ClassPathResource("cv/" + fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + fileName + "\"")
                .body(file);
    }

}
