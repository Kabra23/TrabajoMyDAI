package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.model.Recordatorio;
import com.example.TrabajoMyDAI.data.model.Ticket;
import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.repository.RecordatorioRepository;
import com.example.TrabajoMyDAI.data.repository.TicketRepository;
import com.example.TrabajoMyDAI.data.services.RecordatorioService;
import com.example.TrabajoMyDAI.data.exceptions.ValidationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Controller
@RequestMapping("/recordatorios")
public class RecordatorioController {

    private final RecordatorioRepository recordatorioRepository;
    private final TicketRepository ticketRepository;
    private final RecordatorioService recordatorioService;

    public RecordatorioController(RecordatorioRepository recordatorioRepository,
                                  TicketRepository ticketRepository,
                                  RecordatorioService recordatorioService) {
        this.recordatorioRepository = recordatorioRepository;
        this.ticketRepository = ticketRepository;
        this.recordatorioService = recordatorioService;
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
        // pasar la fecha mínima (hoy) formateada como yyyy-MM-dd para usarla en el input date
        model.addAttribute("minDate", LocalDate.now().toString());
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

        try {
            // usar el servicio para validar fecha actual y fecha del evento, y guardar
            recordatorioService.crearRecordatorio(recordatorioForm, ticketId, usuario);
        } catch (ValidationException ve) {
            // preparar datos para volver a la vista de creación con el error
            List<Ticket> tickets = ticketRepository.findByUsuario(usuario);
            model.addAttribute("tickets", tickets);
            model.addAttribute("recordatorioForm", recordatorioForm);
            model.addAttribute("ticketId", ticketId);
            model.addAttribute("error", ve.getMessage());
            // asegurar minDate también cuando re-renderizamos la vista con error
            model.addAttribute("minDate", java.time.LocalDate.now().toString());
            return "recordatorios/crear";
        }

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
