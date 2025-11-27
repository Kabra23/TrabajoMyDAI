package com.example.TrabajoMyDAI.TrabajoMyDAI;

import com.example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.model.Recordatorio;
import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.repository.RecordatorioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class RecordatorioRepositoryTest {
    @Autowired
    private TestEntityManager em;

    @Autowired
    private RecordatorioRepository recordatorioRepository;

    @Test
    void testCreateRecordatorio() {
        Usuario u = new Usuario();
        u.setNombre("Luis");
        u.setEmail("luis@example.com");
        em.persist(u);

        Evento e = new Evento();
        e.setNombre("Seminario");
        em.persist(e);

        Recordatorio r = new Recordatorio();
        r.setUsuario(u);
        r.setEvento(e);
        r.setMensaje("Recordar inscripción");
        r.setFecha(LocalDateTime.parse("2025-11-01T00:00"));

        Recordatorio saved = recordatorioRepository.save(r);
        assertNotNull(saved.getId_recordatorio());
        assertEquals("Recordar inscripción", saved.getMensaje());
        assertEquals(u.getDni(), saved.getUsuario().getDni());
        assertEquals(e.getId(), saved.getEvento().getId());
    }

    @Test
    void testReadRecordatorio() {
        Usuario u = new Usuario();
        u.setNombre("Luis");
        u.setEmail("luis@example.com");
        em.persist(u);

        Evento e = new Evento();
        e.setNombre("Seminario");
        em.persist(e);

        Recordatorio r = new Recordatorio();
        r.setUsuario(u);
        r.setEvento(e);
        r.setMensaje("Recordar inscripción");
        r.setFecha(LocalDateTime.parse("2025-11-01T00:00"));
        Recordatorio saved = em.persistFlushFind(r);

        Optional<Recordatorio> found = recordatorioRepository.findById(saved.getId_recordatorio());
        assertTrue(found.isPresent());
        assertEquals(saved.getId_recordatorio(), found.get().getId_recordatorio());
        assertEquals("Recordar inscripción", found.get().getMensaje());
    }

    @Test
    void testReadAllRecordatorios() {
        Usuario u = new Usuario();
        u.setNombre("Luis");
        u.setEmail("luis@example.com");
        em.persist(u);

        Evento e = new Evento();
        e.setNombre("Seminario");
        em.persist(e);

        Recordatorio r1 = new Recordatorio();
        r1.setUsuario(u);
        r1.setEvento(e);
        r1.setMensaje("Recordar inscripción");
        r1.setFecha(LocalDateTime.parse("2025-11-01T00:00"));
        recordatorioRepository.save(r1);

        Recordatorio r2 = new Recordatorio();
        r2.setUsuario(u);
        r2.setEvento(e);
        r2.setMensaje("Otro recordatorio");
        r2.setFecha(LocalDateTime.parse("2025-11-02T00:00"));
        recordatorioRepository.save(r2);

        Iterable<Recordatorio> recordatorios = recordatorioRepository.findAll();
        List<Recordatorio> recordatorioList = (List<Recordatorio>) recordatorios;
        assertTrue(recordatorioList.size() >= 2);
    }

    @Test
    void testUpdateRecordatorio() {
        Usuario u = new Usuario();
        u.setNombre("Luis");
        u.setEmail("luis@example.com");
        em.persist(u);

        Evento e = new Evento();
        e.setNombre("Seminario");
        em.persist(e);

        Recordatorio r = new Recordatorio();
        r.setUsuario(u);
        r.setEvento(e);
        r.setMensaje("Recordar inscripción");
        r.setFecha(LocalDateTime.parse("2025-11-01T00:00"));
        Recordatorio saved = recordatorioRepository.save(r);

        saved.setMensaje("Recordar pago");
        Recordatorio updated = recordatorioRepository.save(saved);

        assertEquals("Recordar pago", updated.getMensaje());
        Optional<Recordatorio> found = recordatorioRepository.findById(saved.getId_recordatorio());
        assertTrue(found.isPresent());
        assertEquals("Recordar pago", found.get().getMensaje());
    }

    @Test
    void testDeleteRecordatorio() {
        Usuario u = new Usuario();
        u.setNombre("Luis");
        u.setEmail("luis@example.com");
        em.persist(u);

        Evento e = new Evento();
        e.setNombre("Seminario");
        em.persist(e);

        Recordatorio r = new Recordatorio();
        r.setUsuario(u);
        r.setEvento(e);
        r.setMensaje("Recordar inscripción");
        r.setFecha(LocalDateTime.parse("2025-11-01T00:00"));
        Recordatorio saved = recordatorioRepository.save(r);

        recordatorioRepository.deleteById(saved.getId_recordatorio());

        Optional<Recordatorio> found = recordatorioRepository.findById(saved.getId_recordatorio());
        assertFalse(found.isPresent());
    }

    @Test
    void crudRecordatorio() {
        Usuario u = new Usuario();
        u.setNombre("Luis");
        u.setEmail("luis@example.com");
        em.persist(u);

        Evento e = new Evento();
        e.setNombre("Seminario");
        em.persist(e);

        Recordatorio r = new Recordatorio();
        r.setUsuario(u);
        r.setEvento(e);
        r.setMensaje("Recordar inscripción");
        r.setFecha(LocalDateTime.parse("2025-11-01T00:00"));

        Recordatorio saved = em.persistFlushFind(r);
        assertNotNull(saved.getId_recordatorio());
        assertEquals("Recordar inscripción", saved.getMensaje());

        em.remove(saved);
        em.flush();
        Recordatorio deleted = em.find(Recordatorio.class, saved.getId_recordatorio());
        assertNull(deleted);
    }
}