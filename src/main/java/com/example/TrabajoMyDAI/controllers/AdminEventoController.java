package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.model. Evento;
import com.example. TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data. repository.EventoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.LinkedList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminEventoController {

    private final EventoRepository eventoRepository;

    public AdminEventoController(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    // Verificar si el usuario es admin
    private boolean esAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        return usuario != null && usuario.isAdmin();
    }

    @GetMapping("/eventos")
    public String listarEventos(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        LinkedList<Evento> eventos = new LinkedList<>();
        eventoRepository.findAll().forEach(eventos::add);
        model.addAttribute("eventos", eventos);
        model.addAttribute("esAdmin", true);
        model.addAttribute("logueado", true);
        return "admin-eventos";
    }

    @GetMapping("/eventos/crear")
    public String crearEventoForm(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        model.addAttribute("evento", new Evento());
        model.addAttribute("esAdmin", true);
        model.addAttribute("logueado", true);
        return "admin-crear-evento";
    }

    @PostMapping("/eventos/crear")
    public String crearEvento(@ModelAttribute Evento evento, HttpSession session, Model model) {
        if (! esAdmin(session)) {
            return "redirect:/";
        }

        try {
            eventoRepository.save(evento);
            return "redirect:/admin/eventos? success=creado";
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear el evento: " + e.getMessage());
            model.addAttribute("evento", evento);
            model. addAttribute("esAdmin", true);
            model.addAttribute("logueado", true);
            return "admin-crear-evento";
        }
    }

    @GetMapping("/eventos/eliminar/{id}")
    public String eliminarEvento(@PathVariable Long id, HttpSession session) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        eventoRepository.deleteById(id);
        return "redirect:/admin/eventos?success=eliminado";
    }
}