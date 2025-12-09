package com.example.TrabajoMyDAI.data.init;

import com.example.TrabajoMyDAI.data.model.Jugador;
import com.example.TrabajoMyDAI.data.repository.JugadorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JugadorDataLoader implements CommandLineRunner {

    private final JugadorRepository jugadorRepository;

    public JugadorDataLoader(JugadorRepository jugadorRepository) {
        this.jugadorRepository = jugadorRepository;
    }

    @Override
    public void run(String... args) {
        // Si ya hay jugadores, no cargamos nada para evitar duplicados
        if (jugadorRepository.count() > 0) {
            return;
        }

        String defaultImg = "/img/jugadores/placeholder.png";

        List<Jugador> jugadores = List.of(
                // PORTEROS
                new Jugador("Diego Kochen", "Portero", 1, 19, "Estados Unidos", "/img/jugadores/kochen.png"),
                new Jugador("Eder Aller", "Portero", 13, 18, "España", "/img/jugadores/aller.png"),
                new Jugador("Emilio Bernad", "Portero", 25, 26, "España", "/img/jugadores/bernard.png"),

                // DEFENSAS
                new Jugador("Joan Anaya", "Defensa", 2, 20, "España", "/img/jugadores/anaya.png"),
                new Jugador("Alexis Olmedo", "Defensa", 3, 19, "España", "/img/jugadores/olmedo.png"),
                new Jugador("Álvaro Cortés", "Defensa", 4, 20, "España", "/img/jugadores/cortes.png"),
                new Jugador("Andrés Cuenca", "Defensa", 5, 18, "España", "/img/jugadores/cuenca.png"),
                new Jugador("Mamadou Mbacke", "Defensa", 12, 23, "Senegal", "/img/jugadores/mbacke.png"),
                new Jugador("Alex Walton", "Defensa", 15, 19, "España", "/img/jugadores/walton.png"),
                new Jugador("Jofre Torrents", "Defensa", 17, 18, "España", "/img/jugadores/torrents.png"),
                new Jugador("David Oduro", "Defensa", 18, 19, "Ghana", "/img/jugadores/oduro.png"),
                new Jugador("Xavi Espart", "Defensa", 22, 18, "España", "/img/jugadores/espart.png"),
                new Jugador("Landry Farré", "Defensa", 23, 18, "España / Costa de Marfil", "/img/jugadores/farre.png"),

                // CENTROCAMPISTAS
                new Jugador("Roger Martínez", "Centrocampista", 6, 21, "España", "/img/jugadores/martinez.png"),
                new Jugador("Brian Fariñas", "Centrocampista", 8, 19, "España", "/img/jugadores/farinas.png"),
                new Jugador("Guillermo Fernández", "Centrocampista", 10, 17, "España", "/img/jugadores/fernandez.png"),
                new Jugador("Tommy Marqués", "Centrocampista", 14, 19, "España", "/img/jugadores/marques.png"),
                new Jugador("Marcos Parriego", "Centrocampista", 20, 19, "España", "/img/jugadores/parriego.png"),
                new Jugador("Ibrahim Diarra", "Centrocampista", 24, 18, "Mali", "/img/jugadores/diarra.png"),

                // DELANTEROS
                new Jugador("Dani Rodríguez", "Delantero", 7, 20, "España", "/img/jugadores/rodriguez.png"),
                new Jugador("Víctor Barberá", "Delantero", 9, 21, "España", "/img/jugadores/barbera.png"),
                new Jugador("Óscar Ureña", "Delantero", 11, 22, "República Dominicana / España", "/img/jugadores/urena.png"),
                new Jugador("Toni Fernández", "Delantero", 16, 17, "España", "/img/jugadores/toni.png"),
                new Jugador("Abdul Aziz Issah", "Delantero", 19, 20, "Ghana", "/img/jugadores/issah.png")
        );

        jugadorRepository.saveAll(jugadores);
    }
}


