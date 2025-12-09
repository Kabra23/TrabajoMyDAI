package com.example.TrabajoMyDAI.data.init;

import com.example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.repository.EventoRepository;
import com.example.TrabajoMyDAI.data.services.ZonaService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EventoDataLoader {
    @Component
    public static class EventoLoader implements CommandLineRunner {

        private final EventoRepository eventoRepository;
        private final ZonaService zonaService;

        public EventoLoader(EventoRepository eventoRepository, ZonaService zonaService) {
            this.eventoRepository = eventoRepository;
            this.zonaService = zonaService;
        }

        @Override
        public void run(String... args) {

            if (eventoRepository.count() > 0) {
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            // Cargar datos iniciales de eventos
            Evento e1 = new Evento();
            e1.setNombre("Barça Atlètic vs Real Madrid Castilla");
            e1.setDescripcion("Partido de Primera Federación en el Estadi Johan Cruyff.");
            e1.setFecha(LocalDateTime.parse("2025-12-24 19:00", formatter));
            e1.setLugar("Estadi Johan Cruyff");
            e1.setTipo("Liga");

            Evento e2 = new Evento();
            e2.setNombre("Barça Atlètic vs Nàstic de Tarragona");
            e2.setDescripcion("Derbi catalán con ambiente familiar y promociones especiales.");
            e2.setFecha(LocalDateTime.parse("2025-12-25 17:00", formatter));
            e2.setLugar("Estadi Johan Cruyff");
            e2.setTipo("Liga");

            Evento e3 = new Evento();
            e3.setNombre("Barça Atlètic vs Selecció Juvenil");
            e3.setDescripcion("Partido amistoso de presentación de la temporada.");
            e3.setFecha(LocalDateTime.parse("2025-12-26 20:30", formatter));
            e3.setLugar("Ciutat Esportiva Joan Gamper");
            e3.setTipo("Amistoso");

            Evento evento1 = eventoRepository.save(e1);
            Evento evento2 = eventoRepository.save(e2);
            Evento evento3 = eventoRepository.save(e3);

            // Crear zonas para cada evento
            zonaService.crearZonasParaEvento(evento1);
            zonaService.crearZonasParaEvento(evento2);
            zonaService.crearZonasParaEvento(evento3);
        }
    }
}