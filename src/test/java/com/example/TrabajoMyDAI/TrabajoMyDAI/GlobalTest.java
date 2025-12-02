// src/test/java/com/example/TrabajoMyDAI/RecordatorioRepositoryTest.java
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class GlobalTest {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private RecordatorioRepository recordatorioRepository;

    @Test
    void fullCrudWithRelations() {
        // CREATE: Usuario
        Usuario u = new Usuario();
        u.setNombre("Luis");
        u.setEmail("luis@example.com");
        Usuario savedUsuario = usuarioRepository.save(u);

        // CREATE: Evento
        Evento e = new Evento();
        e.setNombre("Seminario");
        e.setDescripcion("Descripción del seminario");
        Evento savedEvento = eventoRepository.save(e);

        // CREATE: Recordatorio con relaciones a Usuario y Evento
        Recordatorio r = new Recordatorio();
        r.setUsuario(savedUsuario);
        r.setEvento(savedEvento);
        r.setMensaje("Recordar inscripción");
        r.setFecha(LocalDateTime.parse("2025-11-01T00:00"));
        Recordatorio saved = recordatorioRepository.save(r);
        recordatorioRepository.flush();

        // READ: comprobaciones básicas y relaciones
        Optional<Recordatorio> foundOpt = recordatorioRepository.findById(saved.getId_recordatorio());
        assertTrue(foundOpt.isPresent());
        Recordatorio found = foundOpt.get();
        assertNotNull(found);
        assertNotNull(found.getId_recordatorio());
        assertEquals("Recordar inscripción", found.getMensaje());
        assertNotNull(found.getFecha());
        assertNotNull(found.getUsuario());
        assertNotNull(found.getEvento());
        assertEquals("Luis", found.getUsuario().getNombre());
        assertEquals("Seminario", found.getEvento().getNombre());

        // UPDATE: modificar mensaje, nombre de usuario y nombre del evento
        found.setMensaje("Nuevo mensaje de recordatorio");
        found.getUsuario().setNombre("Luis Actualizado");
        found.getEvento().setNombre("Seminario Actualizado");
        recordatorioRepository.save(found);
        usuarioRepository.save(found.getUsuario());
        eventoRepository.save(found.getEvento());
        recordatorioRepository.flush();

        // READ: volver a cargar y comprobar cambios
        Optional<Recordatorio> updatedOpt = recordatorioRepository.findById(saved.getId_recordatorio());
        assertTrue(updatedOpt.isPresent());
        Recordatorio updated = updatedOpt.get();
        assertNotNull(updated);
        assertEquals("Nuevo mensaje de recordatorio", updated.getMensaje());
        assertEquals("Luis Actualizado", updated.getUsuario().getNombre());
        assertEquals("Seminario Actualizado", updated.getEvento().getNombre());

        // DELETE: eliminar el recordatorio
        recordatorioRepository.deleteById(updated.getId_recordatorio());
        recordatorioRepository.flush();
        Optional<Recordatorio> deletedOpt = recordatorioRepository.findById(saved.getId_recordatorio());
        assertFalse(deletedOpt.isPresent());

        // COMPROBAR que Usuario y Evento siguen existiendo (no cascade delete esperado)
        Optional<Usuario> persistedUserOpt = usuarioRepository.findById(savedUsuario.getDni());
        Optional<Evento> persistedEventOpt = eventoRepository.findById(savedEvento.getId());

        assertTrue(persistedUserOpt.isPresent(), "El usuario debe seguir existiendo tras borrar el recordatorio");
        assertTrue(persistedEventOpt.isPresent(), "El evento debe seguir existiendo tras borrar el recordatorio");
        assertEquals("Luis Actualizado", persistedUserOpt.get().getNombre());
        assertEquals("Seminario Actualizado", persistedEventOpt.get().getNombre());
    }
}
