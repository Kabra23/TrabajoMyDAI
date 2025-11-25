package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.model.Ticket;
import com.example.TrabajoMyDAI.data.repository.EventoRepository;
import com.example.TrabajoMyDAI.data.repository.RecordatorioRepository;
import com.example.TrabajoMyDAI.data.repository.TicketRepository;
import com.example.TrabajoMyDAI.data.repository.UsuarioRepository;
import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.services.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;


import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class HomeController {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TicketRepository ticketRepository;
    private final RecordatorioRepository recordatorioRepository;
    private final UsuarioService usuarioService;

    public HomeController(EventoRepository eventoRepository,
                          UsuarioRepository usuarioRepository,
                          TicketRepository ticketRepository,
                          RecordatorioRepository recordatorioRepository,
                          UsuarioService usuarioService) {
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ticketRepository = ticketRepository;
        this.recordatorioRepository = recordatorioRepository;
        this.usuarioService = usuarioService;
    }

    @GetMapping()
    public String home(Model model, HttpSession session) {
        model.addAttribute("mensaje", "¡Bienvenido a Barça Athletic Hub!");
        model.addAttribute("logueado", session.getAttribute("usuario") != null);
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        return "login";
    }

    @GetMapping("/registro")
    public String registroPage(Model model) {
        return "registro";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findByUsername(username);
        if (optionalUsuario.isPresent() && optionalUsuario.get().getPassword().equals(password)) {
            session.setAttribute("usuario", optionalUsuario.get());
            return "redirect:/";
        }
        model.addAttribute("error", "Credenciales inválidas");
        return "login";
    }

    @PostMapping("/registro")
    public String registro(@RequestParam String nombre,
                          @RequestParam String username,
                          @RequestParam String email,
                          @RequestParam String password,
                          Model model) {
        try {
            if (usuarioRepository.findByUsername(username).isPresent()) {
                model.addAttribute("error", "El usuario ya existe");
                return "registro";
            }
            Usuario nuevo = new Usuario();
            nuevo.setNombre(nombre);
            nuevo.setUsername(username);
            nuevo.setEmail(email);
            nuevo.setPassword(password);
            nuevo.setRoles("USER");
            usuarioService.guardar(nuevo);
            model.addAttribute("registroExitoso", true);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "registro";
    }

    @GetMapping("/noticias")
    public String noticias(Model model) {
        // Por ahora mostramos los eventos como "noticias" (puedes crear una entidad Noticia más adelante)
        var eventos = eventoRepository.findAll();
        model.addAttribute("noticias", eventos);
        model.addAttribute("mensaje", "Últimas noticias");
        return "noticias";
    }

    @GetMapping("/eventos")
    public String eventos(Model model) {
        var eventos = eventoRepository.findAll();
        model.addAttribute("eventos", eventos);
        model.addAttribute("mensaje", "Lista de eventos");
        return "eventos";
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

    @GetMapping("/tickets")
    public String tickets(Model model, HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/";
        }
        var tickets = ticketRepository.findAll();
        model.addAttribute("tickets", tickets);
        model.addAttribute("mensaje", "Lista de tickets");
        return "tickets";
    }

    @GetMapping("/recordatorios")
    public String recordatorios(Model model, HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/";
        }
        var recordatorios = recordatorioRepository.findAll();
        model.addAttribute("recordatorios", recordatorios);
        model.addAttribute("mensaje", "Lista de recordatorios");
        return "recordatorios";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/eventos/{id}/comprar")
    public String mostrarFormularioCompra(@PathVariable("id") Long id,
                                          Model model,
                                          HttpSession session) {
        // Requerimos usuario logueado para comprar
        if (session.getAttribute("usuario") == null) {
            return "redirect:/login";
        }

        var evento = eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        model.addAttribute("evento", evento);
        // Zonas de ejemplo
        model.addAttribute("zonas", java.util.List.of("Tribuna", "Grada Lateral", "Gol Nord", "Gol Sud"));
        return "comprar-ticket";
    }

    @PostMapping("/eventos/{id}/comprar")
    public String procesarCompra(@PathVariable("id") Long id,
                                 @RequestParam String zona,
                                 @RequestParam Long asiento,
                                 HttpSession session,
                                 Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        // Precio simple según la zona (puedes cambiarlo)
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

        // Después de comprar, lo mandamos a la lista de tickets
        return "redirect:/tickets";
    }
}
