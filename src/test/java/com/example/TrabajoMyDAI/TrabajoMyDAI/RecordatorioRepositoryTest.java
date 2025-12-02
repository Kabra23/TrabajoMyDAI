package com.example.TrabajoMyDAI.TrabajoMyDAI;

import com.example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.model.Recordatorio;
import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.repository.EventoRepository;
import com.example.TrabajoMyDAI.data.repository.RecordatorioRepository;
import com.example.TrabajoMyDAI.data.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class RecordatorioRepositoryTest {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private RecordatorioRepository recordatorioRepository;

    @Test
    void testCreateRecordatorio() {
        Usuario u = new Usuario();
        u.setNombre("Luis");
        u.setEmail("luis@example.com");
        Usuario savedUsuario = usuarioRepository.save(u);

        Evento e = new Evento();
        e.setNombre("Seminario");
        Evento savedEvento = eventoRepository.save(e);

        Recordatorio r = new Recordatorio();
        r.setUsuario(savedUsuario);
        r.setEvento(savedEvento);
        r.setMensaje("Recordar inscripción");
        r.setFecha(LocalDateTime.parse("2025-11-01T00:00"));

        Recordatorio saved = recordatorioRepository.save(r);
        assertNotNull(saved.getId_recordatorio());
        assertEquals("Recordar inscripción", saved.getMensaje());
        assertEquals(savedUsuario.getDni(), saved.getUsuario().getDni());
        assertEquals(savedEvento.getId(), saved.getEvento().getId());
    }

    @Test
    void testReadRecordatorio() {
        Usuario u = new Usuario();
        u.setNombre("Luis");
        u.setEmail("luis@example.com");
        Usuario savedUsuario = usuarioRepository.save(u);

        Evento e = new Evento();
        e.setNombre("Seminario");
        Evento savedEvento = eventoRepository.save(e);

        Recordatorio r = new Recordatorio();
        r.setUsuario(savedUsuario);
        r.setEvento(savedEvento);
        r.setMensaje("Recordar inscripción");
        r.setFecha(LocalDateTime.parse("2025-11-01T00:00"));
        Recordatorio saved = recordatorioRepository.save(r);
        recordatorioRepository.flush();

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
        Usuario savedUsuario = usuarioRepository.save(u);

        Evento e = new Evento();
        e.setNombre("Seminario");
        Evento savedEvento = eventoRepository.save(e);

        Recordatorio r1 = new Recordatorio();
        r1.setUsuario(savedUsuario);
        r1.setEvento(savedEvento);
        r1.setMensaje("Recordar inscripción");
        r1.setFecha(LocalDateTime.parse("2025-11-01T00:00"));
        recordatorioRepository.save(r1);

        Recordatorio r2 = new Recordatorio();
        r2.setUsuario(savedUsuario);
        r2.setEvento(savedEvento);
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
        Usuario savedUsuario = usuarioRepository.save(u);

        Evento e = new Evento();
        e.setNombre("Seminario");
        Evento savedEvento = eventoRepository.save(e);

        Recordatorio r = new Recordatorio();
        r.setUsuario(savedUsuario);
        r.setEvento(savedEvento);
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
        Usuario savedUsuario = usuarioRepository.save(u);

        Evento e = new Evento();
        e.setNombre("Seminario");
        Evento savedEvento = eventoRepository.save(e);

        Recordatorio r = new Recordatorio();
        r.setUsuario(savedUsuario);
        r.setEvento(savedEvento);
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
        Usuario savedUsuario = usuarioRepository.save(u);

        Evento e = new Evento();
        e.setNombre("Seminario");
        Evento savedEvento = eventoRepository.save(e);

        Recordatorio r = new Recordatorio();
        r.setUsuario(savedUsuario);
        r.setEvento(savedEvento);
        r.setMensaje("Recordar inscripción");
        r.setFecha(LocalDateTime.parse("2025-11-01T00:00"));

        Recordatorio saved = recordatorioRepository.save(r);
        recordatorioRepository.flush();
        Optional<Recordatorio> foundOpt = recordatorioRepository.findById(saved.getId_recordatorio());
        assertTrue(foundOpt.isPresent());
        assertNotNull(foundOpt.get().getId_recordatorio());
        assertEquals("Recordar inscripción", foundOpt.get().getMensaje());

        recordatorioRepository.deleteById(saved.getId_recordatorio());
        recordatorioRepository.flush();
        Optional<Recordatorio> deletedOpt = recordatorioRepository.findById(saved.getId_recordatorio());
        assertFalse(deletedOpt.isPresent());
    }
}