package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.model.Zona;
import com.example.TrabajoMyDAI.data.services.EventoService;
import com.example.TrabajoMyDAI.data.services.ZonaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class EventoController {

    private final EventoService eventoService;
    private final ZonaService zonaService;

    public EventoController(EventoService eventoService, ZonaService zonaService) {
        this.eventoService = eventoService;
        this.zonaService = zonaService;
    }

    @GetMapping("/noticias")
    public String noticias(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        model.addAttribute("logueado", usuario != null);
        model.addAttribute("esAdmin", usuario != null && usuario.isAdmin());

        model.addAttribute("noticias", eventoService.obtenerTodosLosEventos());
        model.addAttribute("mensaje", "Últimas noticias");
        return "noticias";
    }

    @GetMapping("/eventos")
    public String eventos(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        model.addAttribute("logueado", usuario != null);
        model.addAttribute("esAdmin", usuario != null && usuario.isAdmin());

        model.addAttribute("eventos", eventoService.obtenerEventosFuturos());
        model.addAttribute("mensaje", "Lista de eventos");
        return "eventos";
    }

    @GetMapping("/eventos/{id}/comprar")
    public String mostrarFormularioCompra(@PathVariable("id") Long id, Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        model.addAttribute("logueado", usuario != null);

        // Verificar si el usuario es admin y bloquear acceso a compra
        if (usuario != null && usuario.isAdmin()) {
            model.addAttribute("esAdmin", true);
            model.addAttribute("error", "Los administradores no pueden comprar entradas");
            return "redirect:/eventos?error=admin_no_puede_comprar";
        }

        var evento = eventoService.obtenerEventoPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        model.addAttribute("evento", evento);

        // Obtener zonas con su disponibilidad
        List<Zona> zonasEvento = zonaService.obtenerZonasPorEvento(id);
        model.addAttribute("zonasDisponibilidad", zonasEvento);

        // Si no hay zonas creadas, crear las zonas predeterminadas
        if (zonasEvento.isEmpty()) {
            zonaService.crearZonasParaEvento(evento);
            zonasEvento = zonaService.obtenerZonasPorEvento(id);
            model.addAttribute("zonasDisponibilidad", zonasEvento);
        }

        // Lista de nombres de zonas para el select
        model.addAttribute("zonas", zonasEvento.stream().map(Zona::getNombre).toList());

        // Calcular entradas disponibles totales
        Integer entradasDisponiblesTotales = zonaService.obtenerEntradasDisponiblesTotales(id);
        if (entradasDisponiblesTotales != null) {
            model.addAttribute("plazasDisponibles", entradasDisponiblesTotales);
            model.addAttribute("tieneCapacidad", true);
        } else {
            model.addAttribute("tieneCapacidad", false);
        }

        model.addAttribute("esAdmin", false);
        return "comprar-ticket";
    }

    @GetMapping("/eventos/{id}/comprar-3d")
    public String mostrarFormularioCompra3D(@PathVariable("id") Long id, Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        model.addAttribute("logueado", usuario != null);

        // Verificar si el usuario es admin y bloquear acceso a compra
        if (usuario != null && usuario.isAdmin()) {
            model.addAttribute("esAdmin", true);
            model.addAttribute("error", "Los administradores no pueden comprar entradas");
            return "redirect:/eventos?error=admin_no_puede_comprar";
        }

        var evento = eventoService.obtenerEventoPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        model.addAttribute("evento", evento);

        // Obtener zonas con su disponibilidad
        List<Zona> zonasEvento = zonaService.obtenerZonasPorEvento(id);

        // Si no hay zonas creadas, crear las zonas predeterminadas
        if (zonasEvento.isEmpty()) {
            zonaService.crearZonasParaEvento(evento);
        }

        model.addAttribute("esAdmin", false);
        return "comprar-tickets-3d";
    }
}
