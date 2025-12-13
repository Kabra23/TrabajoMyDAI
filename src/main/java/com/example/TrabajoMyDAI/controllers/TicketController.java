package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.exceptions.ValidationException;
import com.example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.model.Ticket;
import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.model.Zona;
import com.example.TrabajoMyDAI.data.repository.EventoRepository;
import com.example.TrabajoMyDAI.data.repository.TicketRepository;
import com.example.TrabajoMyDAI.data.services.ZonaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class TicketController {

    private final TicketRepository ticketRepository;
    private final EventoRepository eventoRepository;
    private final ZonaService zonaService;

    public TicketController(TicketRepository ticketRepository, EventoRepository eventoRepository, ZonaService zonaService) {
        this.ticketRepository = ticketRepository;
        this.eventoRepository = eventoRepository;
        this.zonaService = zonaService;
    }

    @GetMapping("/tickets")
    public String tickets(Model model, HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/";
        }
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        var tickets = ticketRepository.findByUsuario(usuario);
        model.addAttribute("tickets", tickets);
        model.addAttribute("mensaje", "Mis Tickets");
        model.addAttribute("logueado", true);  // ✅ AÑADIDO
        model.addAttribute("esAdmin", usuario.isAdmin());  // ✅ AÑADIDO
        return "tickets";
    }

    @PostMapping("/eventos/{id}/comprar")
    public String procesarCompra(@PathVariable("id") Long id,
                                 @RequestParam String zona,
                                 @RequestParam Long asiento,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        // VALIDACIÓN: Verificar que el usuario NO sea administrador
        if (usuario.isAdmin()) {
            redirectAttributes.addFlashAttribute("error",
                    "Los administradores no pueden comprar entradas.");
            return "redirect:/eventos";
        }

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        // VALIDACIÓN: Verificar disponibilidad de la zona
        Zona zonaSeleccionada = zonaService.obtenerZonaPorEventoYNombre(id, zona)
                .orElseThrow(() -> new ValidationException("Zona no encontrada."));

        // VALIDACIÓN: Verificar si el asiento ya está ocupado EN ESTA ZONA ESPECÍFICA
        if (ticketRepository.findByZonaAndAsiento(zonaSeleccionada, asiento).isPresent()) {
            redirectAttributes.addFlashAttribute("error",
                    "El asiento " + asiento + " ya está ocupado en la zona " + zona + ". Por favor, selecciona otro.");
            return "redirect:/eventos/" + id + "/comprar";
        }

        if (!zonaSeleccionada.hayDisponibilidad()) {
            redirectAttributes.addFlashAttribute("error",
                    "Lo sentimos, no hay entradas disponibles en la zona " + zona + ".");
            return "redirect:/eventos/" + id + "/comprar";
        }

        double precio = switch (zona) {
            case "Tribuna" -> 35.0;
            case "Grada Lateral" -> 25.0;
            case "Gol Nord", "Gol Sud" -> 15.0;
            default -> 20.0;
        };

        Ticket ticket = new Ticket();
        ticket.setUsuario(usuario);
        ticket.setEvento(evento);
        ticket.setZona(zonaSeleccionada);
        ticket.setAsiento(asiento);
        ticket.setPrecio(precio);

        ticketRepository.save(ticket);

        // Incrementar el contador de entradas vendidas en la zona
        zonaService.incrementarEntradasVendidas(zonaSeleccionada.getId());

        redirectAttributes.addFlashAttribute("success",
                "¡Compra realizada con éxito! Asiento " + asiento + " reservado en zona " + zona + ".");

        return "redirect:/tickets";
    }
}