package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.model.Ticket;
import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.repository.EventoRepository;
import com.example.TrabajoMyDAI.data.repository.TicketRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class TicketController {

    private final TicketRepository ticketRepository;
    private final EventoRepository eventoRepository;

    public TicketController(TicketRepository ticketRepository, EventoRepository eventoRepository) {
        this.ticketRepository = ticketRepository;
        this.eventoRepository = eventoRepository;
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
        return "tickets";
    }

    @PostMapping("/eventos/{id}/comprar")
    public String procesarCompra(@PathVariable("id") Long id,
                                 @RequestParam String zona,
                                 @RequestParam Long asiento,
                                 HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        double precio = switch (zona) {
            case "Tribuna" -> 35.0;
            case "Grada Lateral" -> 25.0;
            case "Gol Nord", "Gol Sud" -> 15.0;
            default -> 20.0;
        };

        Ticket ticket = new Ticket();
        ticket.setUsuario(usuario);
        ticket.setEvento(evento);
        ticket.setAsiento(asiento);
        ticket.setPrecio(precio);

        ticketRepository.save(ticket);

        return "redirect:/tickets";
    }
}

