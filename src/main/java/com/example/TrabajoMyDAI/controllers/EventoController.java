package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.repository.EventoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EventoController {

    private final EventoRepository eventoRepository;

    public EventoController(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @GetMapping("/noticias")
    public String noticias(Model model) {
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

    @GetMapping("/eventos/{id}/comprar")
    public String mostrarFormularioCompra(@PathVariable("id") Long id,
                                          Model model) {
        var evento = eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        model.addAttribute("evento", evento);
        model.addAttribute("zonas", java.util.List.of("Tribuna", "Grada Lateral", "Gol Nord", "Gol Sud"));
        return "comprar-ticket";
    }
}
