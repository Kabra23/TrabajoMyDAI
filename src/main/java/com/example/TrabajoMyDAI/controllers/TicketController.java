package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.exceptions.ValidationException;
import com.example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.model.Ticket;
import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.model.Zona;
import com.example.TrabajoMyDAI.data.repository.EventoRepository;
import com.example.TrabajoMyDAI.data.repository.TicketRepository;
import com.example.TrabajoMyDAI.data.repository.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;

    public TicketController(TicketRepository ticketRepository, EventoRepository eventoRepository,
                           ZonaService zonaService, UsuarioRepository usuarioRepository) {
        this.ticketRepository = ticketRepository;
        this.eventoRepository = eventoRepository;
        this.zonaService = zonaService;
        this.usuarioRepository = usuarioRepository;
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

        // Obtener precio dinámico del evento según la zona
        double precio = evento.getPrecioPorZona(zona);

        // VALIDACIÓN: Verificar que el usuario tenga saldo suficiente
        if (usuario.getSaldo() < precio) {
            redirectAttributes.addFlashAttribute("error",
                    String.format("Saldo insuficiente. Necesitas %.2f€ pero solo tienes %.2f€. Por favor, recarga tu saldo.",
                    precio, usuario.getSaldo()));
            return "redirect:/eventos/" + id + "/comprar";
        }

        // Descontar el precio del saldo del usuario
        if (!usuario.descontarSaldo(precio)) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al procesar el pago. Por favor, inténtalo de nuevo.");
            return "redirect:/eventos/" + id + "/comprar";
        }

        // Guardar usuario con saldo actualizado
        usuarioRepository.save(usuario);
        // Actualizar sesión
        session.setAttribute("usuario", usuario);

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
                String.format("¡Compra realizada con éxito! Asiento %d reservado en zona %s. Se han descontado %.2f€ de tu saldo.",
                asiento, zona, precio));

        return "redirect:/tickets";
    }

    @PostMapping("/tickets/devolver/{id}")
    public String devolverTicket(@PathVariable("id") Long id,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));

        // Verificar que el ticket pertenece al usuario
        if (!ticket.getUsuario().getDni().equals(usuario.getDni())) {
            redirectAttributes.addFlashAttribute("error",
                    "No tienes permiso para devolver este ticket.");
            return "redirect:/tickets";
        }

        // Verificar que el evento no haya pasado
        if (ticket.getEvento().getFecha().isBefore(java.time.LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error",
                    "No se puede devolver un ticket de un evento que ya ha pasado.");
            return "redirect:/tickets";
        }

        // Devolver el dinero al saldo del usuario (si no es admin)
        if (!usuario.isAdmin()) {
            usuario.agregarSaldo(ticket.getPrecio());
            usuarioRepository.save(usuario);  // Guardar en BD
            session.setAttribute("usuario", usuario);  // Actualizar sesión
        }

        // Decrementar entradas vendidas de la zona
        if (ticket.getZona() != null) {
            zonaService.decrementarEntradasVendidas(ticket.getZona().getId());
        }

        // Eliminar el ticket
        ticketRepository.deleteById(id);

        redirectAttributes.addFlashAttribute("success",
                "Ticket devuelto correctamente. Se ha reembolsado " + ticket.getPrecio() + "€ a tu saldo.");

        return "redirect:/tickets";
    }
}