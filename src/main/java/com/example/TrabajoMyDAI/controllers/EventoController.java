package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.services.EventoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping("/noticias")
    public String noticias(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        model.addAttribute("logueado", usuario != null);
        model.addAttribute("esAdmin", usuario != null && usuario.isAdmin());  // ✅ AÑADIR ESTA LÍNEA

        model.addAttribute("noticias", eventoService.obtenerTodosLosEventos());
        model.addAttribute("mensaje", "Últimas noticias");
        return "noticias";
    }

    @GetMapping("/eventos")
    public String eventos(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        model.addAttribute("logueado", usuario != null);
        model. addAttribute("esAdmin", usuario != null && usuario.isAdmin());  // ✅ AÑADIR ESTA LÍNEA

        model.addAttribute("eventos", eventoService.obtenerEventosFuturos());
        model.addAttribute("mensaje", "Lista de eventos");
        return "eventos";
    }
    @GetMapping("/eventos/{id}/comprar")
    public String mostrarFormularioCompra(@PathVariable("id") Long id, Model model, HttpSession session) {
        Usuario usuario = (Usuario) session. getAttribute("usuario");
        model.addAttribute("logueado", usuario != null);

        var evento = eventoService.obtenerEventoPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        model.addAttribute("evento", evento);
        model.addAttribute("zonas", java.util.List.of("Tribuna", "Grada Lateral", "Gol Nord", "Gol Sud"));

        // Agregar información de capacidad si existe
        if (evento.getCapacidad() != null) {
            Integer plazasDisponibles = eventoService.obtenerPlazasDisponibles(id);
            model.addAttribute("plazasDisponibles", plazasDisponibles);
            model.addAttribute("tieneCapacidad", true);
        } else {
            model.addAttribute("tieneCapacidad", false);
        }

        model.addAttribute("esAdmin", usuario != null && usuario.isAdmin());  // ✅ AÑADIR
        return "comprar-ticket";
    }
}