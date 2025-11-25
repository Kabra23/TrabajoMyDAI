package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.model.Recordatorio;
import com.example.TrabajoMyDAI.data.model.Ticket;
import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.repository.RecordatorioRepository;
import com.example.TrabajoMyDAI.data.repository.TicketRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/recordatorios")
public class RecordatorioController {

    private final RecordatorioRepository recordatorioRepository;
    private final TicketRepository ticketRepository;

    public RecordatorioController(RecordatorioRepository recordatorioRepository,
                                  TicketRepository ticketRepository) {
        this.recordatorioRepository = recordatorioRepository;
        this.ticketRepository = ticketRepository;
    }

    @GetMapping
    public String listar(Model model, HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/";
        }
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        List<Recordatorio> recordatorios = recordatorioRepository.findByUsuario(usuario);
        List<Ticket> tickets = ticketRepository.findByUsuario(usuario);
        model.addAttribute("recordatorios", recordatorios);
        model.addAttribute("tickets", tickets);
        model.addAttribute("mensaje", "Lista de recordatorios");
        return "recordatorios/list";
    }

    @GetMapping("/crear")
    public String mostrarCrear(@RequestParam(required = false) Long ticketId, Model model, HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/";
        }
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        List<Ticket> tickets = ticketRepository.findByUsuario(usuario);
        Recordatorio recordatorio = new Recordatorio();
        if (ticketId != null) {
            Optional<Ticket> optionalTicket = ticketRepository.findById(ticketId);
            if (optionalTicket.isPresent() && optionalTicket.get().getUsuario().getDni().equals(usuario.getDni())) {
                recordatorio.setEvento(optionalTicket.get().getEvento());
            }
            model.addAttribute("ticketId", ticketId);
        } else {
            model.addAttribute("ticketId", null);
        }
        model.addAttribute("recordatorioForm", recordatorio);
        model.addAttribute("tickets", tickets);
        model.addAttribute("mensaje", "Crear recordatorio");
        return "recordatorios/crear";
    }

    @PostMapping
    public String crear(@ModelAttribute("recordatorioForm") Recordatorio recordatorioForm,
                        @RequestParam(required = false) Long ticketId,
                        Model model,
                        HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/";
        }
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        // Validate ticket ownership if ticketId provided
        if (ticketId != null) {
            Optional<Ticket> optionalTicket = ticketRepository.findById(ticketId);
            if (optionalTicket.isEmpty() || !optionalTicket.get().getUsuario().getDni().equals(usuario.getDni())) {
                model.addAttribute("error", "Ticket inválido o no pertenece al usuario");
                return "recordatorios/crear";
            }
            recordatorioForm.setEvento(optionalTicket.get().getEvento());
        }

        // Set usuario owner
        recordatorioForm.setUsuario(usuario);

        // Save
        recordatorioRepository.save(recordatorioForm);
        return "redirect:/recordatorios";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/";
        }
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        Optional<Recordatorio> optionalRecordatorio = recordatorioRepository.findById(id);
        if (optionalRecordatorio.isEmpty()) {
            model.addAttribute("error", "Recordatorio no encontrado");
            return "redirect:/recordatorios";
        }
        Recordatorio r = optionalRecordatorio.get();
        if (!r.getUsuario().getDni().equals(usuario.getDni())) {
            model.addAttribute("error", "No tienes permiso para eliminar este recordatorio");
            return "redirect:/recordatorios";
        }
        recordatorioRepository.deleteById(id);
        return "redirect:/recordatorios";
    }
}
