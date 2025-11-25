package com.example.TrabajoMyDAI.data.init;

import com.example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.repository.EventoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

public class EventoDataLoader {
    @Component
    public static class EventoLoader implements CommandLineRunner {

        private final EventoRepository eventoRepository;

        public EventoLoader(EventoRepository eventoRepository) {
            this.eventoRepository = eventoRepository;
        }

        @Override
        public void run(String... args) {

            if (eventoRepository.count() > 0) {
                return;
            }

            // Cargar datos iniciales de eventos
            Evento e1 = new Evento();
            e1.setNombre("Barça Atlètic vs Real Madrid Castilla");
            e1.setDescripcion("Partido de Primera Federación en el Estadi Johan Cruyff.");
            e1.setFecha("2025-02-10 19:00");
            e1.setLugar_evento("Estadi Johan Cruyff");
            e1.setTipo("Liga");

            Evento e2 = new Evento();
            e2.setNombre("Barça Atlètic vs Nàstic de Tarragona");
            e2.setDescripcion("Derbi catalán con ambiente familiar y promociones especiales.");
            e2.setFecha("2025-03-02 17:00");
            e2.setLugar_evento("Estadi Johan Cruyff");
            e2.setTipo("Liga");

            Evento e3 = new Evento();
            e3.setNombre("Barça Atlètic vs Selecció Juvenil");
            e3.setDescripcion("Partido amistoso de presentación de la temporada.");
            e3.setFecha("2025-08-15 20:30");
            e3.setLugar_evento("Ciutat Esportiva Joan Gamper");
            e3.setTipo("Amistoso");

            eventoRepository.save(e1);
            eventoRepository.save(e2);
            eventoRepository.save(e3);
        }
    }
}
