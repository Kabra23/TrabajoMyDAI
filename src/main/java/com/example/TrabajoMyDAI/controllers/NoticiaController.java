package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.model.Noticia;
import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.repository.NoticiaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class NoticiaController {

    private final NoticiaRepository noticiaRepository;

    public NoticiaController(NoticiaRepository noticiaRepository) {
        this.noticiaRepository = noticiaRepository;
    }

    @GetMapping("/noticias")
    public String noticias(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        model.addAttribute("logueado", usuario != null);
        model.addAttribute("esAdmin", usuario != null && usuario.isAdmin());

        model.addAttribute("noticias", noticiaRepository.findAllByOrderByIdDesc());
        return "noticias";
    }

    @GetMapping("/noticias/{id}")
    public String noticiaDetalle(@PathVariable Long id, Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        model.addAttribute("logueado", usuario != null);
        model.addAttribute("esAdmin", usuario != null && usuario.isAdmin());

        Noticia n = noticiaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Noticia no encontrada"));

        model.addAttribute("noticia", n);

        List<String> lineas = List.of();
        if (n.getContenido() != null) {
            lineas = Arrays.stream(n.getContenido().split("\\R", -1))
                    .map(s -> s == null ? "" : s.trim())
                    .collect(Collectors.toList());
        }
        model.addAttribute("lineasContenido", lineas);

        return "noticia-detalle";
    }
}


