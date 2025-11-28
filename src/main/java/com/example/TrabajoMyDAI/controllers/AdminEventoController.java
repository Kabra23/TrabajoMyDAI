package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.exceptions.ValidationException;
import com. example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data. services.EventoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java. time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin")
public class AdminEventoController {

    private final EventoService eventoService;

    public AdminEventoController(EventoService eventoService) {
        this.eventoService = eventoService;
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

        model.addAttribute("eventos", eventoService. obtenerTodosLosEventos());
        model.addAttribute("esAdmin", true);
        model.addAttribute("logueado", true);
        return "admin-eventos";
    }

    @GetMapping("/eventos/crear")
    public String mostrarFormularioCrearEvento(HttpSession session, Model model) {
        if (! esAdmin(session)) {
            return "redirect:/";
        }

        model.addAttribute("evento", new Evento());
        String minFecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        model.addAttribute("minFecha", minFecha);
        model.addAttribute("esAdmin", true);
        model.addAttribute("logueado", true);
        return "admin-crear-evento";
    }

    @PostMapping("/eventos/crear")
    public String crearEvento(@ModelAttribute Evento evento, HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        try {
            eventoService.crearEvento(evento);
            return "redirect:/admin/eventos?success=creado";
        } catch (ValidationException ve) {
            model.addAttribute("error", ve.getMessage());
            model.addAttribute("evento", evento);
            model.addAttribute("esAdmin", true);
            model.addAttribute("logueado", true);
            String minFecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            model.addAttribute("minFecha", minFecha);
            return "admin-crear-evento";
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear el evento: " + e.getMessage());
            model. addAttribute("evento", evento);
            model.addAttribute("esAdmin", true);
            model.addAttribute("logueado", true);
            String minFecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            model.addAttribute("minFecha", minFecha);
            return "admin-crear-evento";
        }
    }

    @GetMapping("/eventos/eliminar/{id}")
    public String eliminarEvento(@PathVariable Long id, HttpSession session, Model model) {
        if (! esAdmin(session)) {
            return "redirect:/";
        }

        try {
            eventoService.eliminarEvento(id);
            return "redirect:/admin/eventos? success=eliminado";
        } catch (ValidationException ve) {
            return "redirect:/admin/eventos?error=" + ve.getMessage();
        } catch (Exception e) {
            return "redirect:/admin/eventos?error=Error al eliminar el evento";
        }
    }

    @GetMapping("/eventos/editar/{id}")
    public String mostrarFormularioEditarEvento(@PathVariable Long id, HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        try {
            Evento evento = eventoService. obtenerEventoPorId(id)
                    .orElseThrow(() -> new ValidationException("Evento no encontrado"));

            model.addAttribute("evento", evento);
            String minFecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            model.addAttribute("minFecha", minFecha);
            model.addAttribute("esAdmin", true);
            model.addAttribute("logueado", true);
            return "admin-editar-evento";
        } catch (ValidationException ve) {
            return "redirect:/admin/eventos?error=" + ve.getMessage();
        }
    }

    @PostMapping("/eventos/editar/{id}")
    public String editarEvento(@PathVariable Long id, @ModelAttribute Evento evento, HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        try {
            eventoService.actualizarEvento(id, evento);
            return "redirect:/admin/eventos?success=actualizado";
        } catch (ValidationException ve) {
            model.addAttribute("error", ve.getMessage());
            model.addAttribute("evento", evento);
            model.addAttribute("esAdmin", true);
            model.addAttribute("logueado", true);
            String minFecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            model.addAttribute("minFecha", minFecha);
            return "admin-editar-evento";
        } catch (Exception e) {
            model. addAttribute("error", "Error al actualizar el evento: " + e.getMessage());
            model.addAttribute("evento", evento);
            model.addAttribute("esAdmin", true);
            model.addAttribute("logueado", true);
            String minFecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            model.addAttribute("minFecha", minFecha);
            return "admin-editar-evento";
        }
    }
}