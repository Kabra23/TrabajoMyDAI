package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.repository.JugadorRepository;
import com.example.TrabajoMyDAI.data.model.Jugador;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PlantillaController {

    private final JugadorRepository jugadorRepository;

    public PlantillaController(JugadorRepository jugadorRepository) {
        this.jugadorRepository = jugadorRepository;
    }

    @GetMapping("/plantilla")
    public String verPlantilla(
        @RequestParam(name = "posicion", required = false) String posicion,
        Model model){

            model.addAttribute("mensaje", "Plantilla del Barça Atlètic");

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

            return "plantilla";
        }
    }


