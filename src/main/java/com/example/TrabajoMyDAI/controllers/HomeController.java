package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.model.Usuario;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario != null) {
            model.addAttribute("mensaje", "¡Bienvenido de nuevo, " + usuario.getNombre() + "!");
            model.addAttribute("logueado", true);
            model.addAttribute("esAdmin", usuario.isAdmin());
        } else {
            model.addAttribute("mensaje", "¡Bienvenido a Barça Athletic Hub!");
            model.addAttribute("logueado", false);
            model.addAttribute("esAdmin", false);
        }

        return "index";
    }
}