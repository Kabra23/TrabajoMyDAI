package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.repository.EventoRepository;
import com.example.TrabajoMyDAI.data.repository.RecordatorioRepository;
import com.example.TrabajoMyDAI.data.repository.TicketRepository;
import com.example.TrabajoMyDAI.data.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TicketRepository ticketRepository;
    private final RecordatorioRepository recordatorioRepository;

    public HomeController(EventoRepository eventoRepository,
                          UsuarioRepository usuarioRepository,
                          TicketRepository ticketRepository,
                          RecordatorioRepository recordatorioRepository) {
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ticketRepository = ticketRepository;
        this.recordatorioRepository = recordatorioRepository;
    }

    @GetMapping()
    public String home(Model model, HttpSession session) {
        model.addAttribute("mensaje", "¡Bienvenido a Barça Athletic Hub!");
        model.addAttribute("logueado", session.getAttribute("usuario") != null);
        return "index";
    }

    @GetMapping("/plantilla")
    public String plantilla(Model model) {
        // Página de plantilla: mostramos algunos contadores de las entidades para relacionar con las clases
        long totalEventos = eventoRepository.count();
        long totalUsuarios = usuarioRepository.count();
        long totalTickets = ticketRepository.count();
        long totalRecordatorios = recordatorioRepository.count();

        model.addAttribute("totalEventos", totalEventos);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("totalRecordatorios", totalRecordatorios);
        model.addAttribute("mensaje", "Panel de información - Plantilla");
        return "plantilla";
    }
}
