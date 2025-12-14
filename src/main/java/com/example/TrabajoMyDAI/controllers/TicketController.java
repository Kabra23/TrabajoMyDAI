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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional // Asegurar atomicidad de la transacción
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

        // VALIDACIÓN: Asiento debe ser positivo
        if (asiento <= 0) {
            redirectAttributes.addFlashAttribute("error",
                    "El número de asiento debe ser mayor que 0.");
            return "redirect:/eventos/" + id + "/comprar";
        }

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        // VALIDACIÓN: Verificar disponibilidad de la zona
        Zona zonaSeleccionada = zonaService.obtenerZonaPorEventoYNombre(id, zona)
                .orElseThrow(() -> new ValidationException("Zona no encontrada."));

        // VALIDACIÓN: Verificar que el asiento esté dentro del rango de la zona
        if (zonaSeleccionada.getCapacidadTotal() != null && asiento > zonaSeleccionada.getCapacidadTotal()) {
            redirectAttributes.addFlashAttribute("error",
                    String.format("El asiento %d no existe en la zona %s. El rango válido es de 1 a %d.",
                            asiento, zona, zonaSeleccionada.getCapacidadTotal()));
            return "redirect:/eventos/" + id + "/comprar";
        }

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

    @PostMapping("/eventos/{id}/comprar-multiple")
    @Transactional
    public String procesarCompraMultiple(@PathVariable("id") Long id,
                                        @RequestParam String zona,
                                        @RequestParam Long asientoInicial,
                                        @RequestParam(defaultValue = "1") Integer cantidad,
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

        // VALIDACIÓN: Cantidad entre 1 y 10
        if (cantidad < 1 || cantidad > 10) {
            redirectAttributes.addFlashAttribute("error",
                    "La cantidad debe estar entre 1 y 10 entradas.");
            return "redirect:/eventos/" + id + "/comprar";
        }

        // VALIDACIÓN: Asiento inicial debe ser positivo
        if (asientoInicial <= 0) {
            redirectAttributes.addFlashAttribute("error",
                    "El número de asiento debe ser mayor que 0.");
            return "redirect:/eventos/" + id + "/comprar";
        }

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        // VALIDACIÓN: Verificar disponibilidad de la zona
        Zona zonaSeleccionada = zonaService.obtenerZonaPorEventoYNombre(id, zona)
                .orElseThrow(() -> new ValidationException("Zona no encontrada."));

        // VALIDACIÓN: Verificar que todos los asientos estén dentro del rango
        Long asientoFinal = asientoInicial + cantidad - 1;
        if (zonaSeleccionada.getCapacidadTotal() != null && asientoFinal > zonaSeleccionada.getCapacidadTotal()) {
            redirectAttributes.addFlashAttribute("error",
                    String.format("Los asientos del %d al %d exceden la capacidad de la zona %s (máximo: %d).",
                            asientoInicial, asientoFinal, zona, zonaSeleccionada.getCapacidadTotal()));
            return "redirect:/eventos/" + id + "/comprar";
        }

        // VALIDACIÓN: Verificar que ningún asiento esté ocupado
        for (long i = 0; i < cantidad; i++) {
            long numeroAsiento = asientoInicial + i;
            if (ticketRepository.findByZonaAndAsiento(zonaSeleccionada, numeroAsiento).isPresent()) {
                redirectAttributes.addFlashAttribute("error",
                        String.format("El asiento %d ya está ocupado en la zona %s. Por favor, selecciona otros asientos.",
                                numeroAsiento, zona));
                return "redirect:/eventos/" + id + "/comprar";
            }
        }

        // VALIDACIÓN: Verificar disponibilidad suficiente
        if (zonaSeleccionada.getEntradasDisponibles() < cantidad) {
            redirectAttributes.addFlashAttribute("error",
                    String.format("Solo quedan %d entradas disponibles en la zona %s.",
                            zonaSeleccionada.getEntradasDisponibles(), zona));
            return "redirect:/eventos/" + id + "/comprar";
        }

        // Obtener precio dinámico del evento según la zona
        double precioUnitario = evento.getPrecioPorZona(zona);
        double precioTotal = precioUnitario * cantidad;

        // VALIDACIÓN: Verificar que el usuario tenga saldo suficiente
        if (usuario.getSaldo() < precioTotal) {
            redirectAttributes.addFlashAttribute("error",
                    String.format("Saldo insuficiente. Necesitas %.2f€ pero solo tienes %.2f€. Por favor, recarga tu saldo.",
                            precioTotal, usuario.getSaldo()));
            return "redirect:/eventos/" + id + "/comprar";
        }

        // Descontar el precio total del saldo del usuario
        if (!usuario.descontarSaldo(precioTotal)) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al procesar el pago. Por favor, inténtalo de nuevo.");
            return "redirect:/eventos/" + id + "/comprar";
        }

        // Guardar usuario con saldo actualizado
        usuarioRepository.save(usuario);
        // Actualizar sesión
        session.setAttribute("usuario", usuario);

        // Crear tickets para cada asiento
        for (long i = 0; i < cantidad; i++) {
            long numeroAsiento = asientoInicial + i;

            Ticket ticket = new Ticket();
            ticket.setUsuario(usuario);
            ticket.setEvento(evento);
            ticket.setZona(zonaSeleccionada);
            ticket.setAsiento(numeroAsiento);
            ticket.setPrecio(precioUnitario);

            ticketRepository.save(ticket);

            // Incrementar el contador de entradas vendidas en la zona
            zonaService.incrementarEntradasVendidas(zonaSeleccionada.getId());
        }

        redirectAttributes.addFlashAttribute("success",
                String.format("¡Compra realizada con éxito! Se han reservado %d entradas (asientos %d-%d) en zona %s. Total: %.2f€",
                        cantidad, asientoInicial, asientoFinal, zona, precioTotal));

        return "redirect:/tickets";
    }

    @PostMapping("/eventos/{id}/comprar-asientos-individuales")
    @Transactional
    public String procesarCompraAsientosIndividuales(@PathVariable("id") Long id,
                                                     @RequestParam String asientos,
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

        // Parsear JSON de asientos usando Jackson
        try {
            ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonArray = objectMapper.readTree(asientos);

            // Validar que no haya más de 10 asientos
            if (jsonArray.size() > 10) {
                redirectAttributes.addFlashAttribute("error",
                        "No puedes comprar más de 10 entradas a la vez.");
                return "redirect:/eventos/" + id + "/comprar-3d";
            }

            // Calcular precio total y validar disponibilidad
            double precioTotal = 0;
            java.util.List<java.util.Map<String, Object>> asientosAComprar = new java.util.ArrayList<>();

            for (JsonNode asientoNode : jsonArray) {
                String zona = asientoNode.get("zona").asText();
                long numeroAsiento = asientoNode.get("asiento").asLong();

                // Validar zona
                Zona zonaSeleccionada = zonaService.obtenerZonaPorEventoYNombre(id, zona)
                        .orElseThrow(() -> new ValidationException("Zona " + zona + " no encontrada."));

                // Validar que el asiento esté en el rango
                if (zonaSeleccionada.getCapacidadTotal() != null && numeroAsiento > zonaSeleccionada.getCapacidadTotal()) {
                    redirectAttributes.addFlashAttribute("error",
                            String.format("El asiento %d excede la capacidad de la zona %s (máximo: %d).",
                                    numeroAsiento, zona, zonaSeleccionada.getCapacidadTotal()));
                    return "redirect:/eventos/" + id + "/comprar-3d";
                }

                // Validar que el asiento no esté ocupado
                if (ticketRepository.findByZonaAndAsiento(zonaSeleccionada, numeroAsiento).isPresent()) {
                    redirectAttributes.addFlashAttribute("error",
                            String.format("El asiento %d ya está ocupado en la zona %s.",
                                    numeroAsiento, zona));
                    return "redirect:/eventos/" + id + "/comprar-3d";
                }

                // Validar disponibilidad
                if (!zonaSeleccionada.hayDisponibilidad()) {
                    redirectAttributes.addFlashAttribute("error",
                            "No hay entradas disponibles en la zona " + zona + ".");
                    return "redirect:/eventos/" + id + "/comprar-3d";
                }

                double precio = evento.getPrecioPorZona(zona);
                precioTotal += precio;

                java.util.Map<String, Object> asientoData = new java.util.HashMap<>();
                asientoData.put("zona", zonaSeleccionada);
                asientoData.put("asiento", numeroAsiento);
                asientoData.put("precio", precio);
                asientosAComprar.add(asientoData);
            }

            // Validar saldo
            if (usuario.getSaldo() < precioTotal) {
                redirectAttributes.addFlashAttribute("error",
                        String.format("Saldo insuficiente. Necesitas %.2f€ pero solo tienes %.2f€.",
                                precioTotal, usuario.getSaldo()));
                return "redirect:/eventos/" + id + "/comprar-3d";
            }

            // Descontar saldo
            if (!usuario.descontarSaldo(precioTotal)) {
                redirectAttributes.addFlashAttribute("error",
                        "Error al procesar el pago.");
                return "redirect:/eventos/" + id + "/comprar-3d";
            }

            // Guardar usuario
            usuarioRepository.save(usuario);
            session.setAttribute("usuario", usuario);

            // Crear tickets
            for (java.util.Map<String, Object> asientoData : asientosAComprar) {
                Zona zona = (Zona) asientoData.get("zona");
                long numeroAsiento = (long) asientoData.get("asiento");
                double precio = (double) asientoData.get("precio");

                Ticket ticket = new Ticket();
                ticket.setUsuario(usuario);
                ticket.setEvento(evento);
                ticket.setZona(zona);
                ticket.setAsiento(numeroAsiento);
                ticket.setPrecio(precio);

                ticketRepository.save(ticket);
                zonaService.incrementarEntradasVendidas(zona.getId());
            }

            redirectAttributes.addFlashAttribute("success",
                    String.format("¡Compra realizada con éxito! Se han reservado %d entradas. Total: %.2f€",
                            asientosAComprar.size(), precioTotal));

            return "redirect:/tickets";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al procesar la compra: " + e.getMessage());
            return "redirect:/eventos/" + id + "/comprar-3d";
        }
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