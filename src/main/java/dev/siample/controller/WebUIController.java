package dev.siample.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebUIController {

    @GetMapping({ "/", "/index" })
    public String index() {
        return "index"; // This returns the index.html template
    }

}
