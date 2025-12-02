package com.example.TrabajoMyDAI.TrabajoMyDAI;

import com.example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.repository.EventoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class EventoRepositoryTest {
    @Autowired
    private EventoRepository eventoRepository;

    @Test
    void testCreateEvento() {
        Evento e = new Evento();
        e.setNombre("Concierto");
        e.setFecha(LocalDateTime.parse("2025-12-01T00:00"));
        e.setLugar("Teatro Principal");
        e.setDescripcion("Un gran concierto");
        e.setTipo("Musical");

        Evento saved = eventoRepository.save(e);
        assertNotNull(saved.getId());
        assertEquals("Concierto", saved.getNombre());
        assertEquals("2025-12-01", saved.getFecha());
    }

    @Test
    void testReadEvento() {
        Evento e = new Evento();
        e.setNombre("Concierto");
        e.setFecha(LocalDateTime.parse("2025-12-01T00:00"));
        e.setLugar("Teatro Principal");
        e.setDescripcion("Un gran concierto");
        e.setTipo("Musical");
        Evento saved = eventoRepository.save(e);

        Optional<Evento> found = eventoRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("Concierto", found.get().getNombre());
    }

    @Test
    void testReadAllEventos() {
        Evento e1 = new Evento();
        e1.setNombre("Concierto");
        e1.setFecha(LocalDateTime.parse("2025-12-01T00:00"));
        e1.setLugar("Teatro Principal");
        e1.setDescripcion("Un gran concierto");
        e1.setTipo("Musical");
        eventoRepository.save(e1);

        Evento e2 = new Evento();
        e2.setNombre("Teatro");
        e2.setFecha(LocalDateTime.parse("2025-12-02T00:00"));
        e2.setLugar("Teatro Secundario");
        e2.setDescripcion("Obra de teatro");
        e2.setTipo("Teatro");
        eventoRepository.save(e2);

        Iterable<Evento> eventos = eventoRepository.findAll();
        List<Evento> eventoList = (List<Evento>) eventos;
        assertTrue(eventoList.size() >= 2);
    }

    @Test
    void testUpdateEvento() {
        Evento e = new Evento();
        e.setNombre("Concierto");
        e.setFecha(LocalDateTime.parse("2025-12-01T00:00"));
        e.setLugar("Teatro Principal");
        e.setDescripcion("Un gran concierto");
        e.setTipo("Musical");
        Evento saved = eventoRepository.save(e);

        saved.setNombre("Concierto Especial");
        Evento updated = eventoRepository.save(saved);

        assertEquals("Concierto Especial", updated.getNombre());
        Optional<Evento> found = eventoRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Concierto Especial", found.get().getNombre());
    }

    @Test
    void testDeleteEvento() {
        Evento e = new Evento();
        e.setNombre("Concierto");
        e.setFecha(LocalDateTime.parse("2025-12-01T00:00"));
        e.setLugar("Teatro Principal");
        e.setDescripcion("Un gran concierto");
        e.setTipo("Musical");
        Evento saved = eventoRepository.save(e);

        eventoRepository.deleteById(saved.getId());

        Optional<Evento> found = eventoRepository.findById(saved.getId());
        assertFalse(found.isPresent());
    }
}
