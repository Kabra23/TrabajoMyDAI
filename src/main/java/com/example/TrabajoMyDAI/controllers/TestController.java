package com.example.TrabajoMyDAI.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    @GetMapping("/test-estadio-directo")
    public String testEstadioDirecto() {
        return "test-estadio-directo";
    }
}

