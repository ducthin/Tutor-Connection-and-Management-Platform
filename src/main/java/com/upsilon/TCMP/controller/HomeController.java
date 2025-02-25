package com.upsilon.TCMP.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"", "/", "/home", "/index"})
    public String home(Model model) {
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        return "about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        return "contact";
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {
        return "privacy";
    }

    @GetMapping("/terms")
    public String terms(Model model) {
        return "terms";
    }
}
