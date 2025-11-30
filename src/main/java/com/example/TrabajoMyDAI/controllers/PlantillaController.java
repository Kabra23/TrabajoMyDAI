package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.repository.EventoRepository;
import com.example.TrabajoMyDAI.data.repository.JugadorRepository;
import com.example.TrabajoMyDAI.data.model.Jugador;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PlantillaController {

    private final JugadorRepository jugadorRepository;
    private final EventoRepository eventoRepository;

    public PlantillaController(JugadorRepository jugadorRepository, EventoRepository eventoRepository) {
        this.jugadorRepository = jugadorRepository;
        this.eventoRepository = eventoRepository;
    }

    @GetMapping("/plantilla")
    public String verPlantilla(
            @RequestParam(name = "posicion", required = false) String posicion,
            Model model,
            HttpSession session) {

        model.addAttribute("mensaje", "Plantilla del Barça Atlètic");

        // Verificar si el usuario está logueado
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        model.addAttribute("logueado", usuario != null);
        model.addAttribute("esAdmin", usuario != null && usuario.isAdmin());
        Iterable<Jugador> jugadores;

        if (posicion == null || posicion.isBlank() ||
                posicion.equalsIgnoreCase("Todos")) {

            jugadores = jugadorRepository.findAll();
            model.addAttribute("posicionSeleccionada", "Todos");
        } else {
            jugadores = jugadorRepository.findByPosicionIgnoreCase(posicion);
            model.addAttribute("posicionSeleccionada", posicion);
        }

        model.addAttribute("jugadores", jugadores);

        Evento proximoEvento = obtenerProximoEvento();
        model.addAttribute("proximoEvento", proximoEvento);

        return "plantilla";
    }

    private Evento obtenerProximoEvento() {
        Evento primero = null;
        for (Evento e : eventoRepository.findAll()) {
            primero = e;
            break;
        }
        return primero;
    }
}