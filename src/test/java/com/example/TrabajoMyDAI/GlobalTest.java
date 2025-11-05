// src/test/java/com/example/TrabajoMyDAI/RecordatorioRepositoryTest.java
package com.example.TrabajoMyDAI;

import com.example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.model.Recordatorio;
import com.example.TrabajoMyDAI.data.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import jakarta.persistence.NoResultException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class GlobalTest {
    @Autowired
    private TestEntityManager em;

    @Test
    void fullCrudWithRelations() {
        // CREATE: Usuario
        Usuario u = new Usuario();
        u.setNombre("Luis");
        u.setEmail("luis@example.com");
        em.persist(u);

        // CREATE: Evento
        Evento e = new Evento();
        e.setNombre("Seminario");
        e.setDescripcion("Descripción del seminario");
        em.persist(e);

        // CREATE: Recordatorio con relaciones a Usuario y Evento
        Recordatorio r = new Recordatorio();
        r.setUsuario(u);
        r.setEvento(e);
        r.setMensaje("Recordar inscripción");
        r.setFecha("2025-11-01");
        Recordatorio saved = em.persistFlushFind(r);

        // READ: comprobaciones básicas y relaciones
        assertNotNull(saved);
        assertNotNull(saved.getId_recordatorio());
        assertEquals("Recordar inscripción", saved.getMensaje());
        assertEquals("2025-11-01", saved.getFecha());
        assertNotNull(saved.getUsuario());
        assertNotNull(saved.getEvento());
        assertEquals("Luis", saved.getUsuario().getNombre());
        assertEquals("Seminario", saved.getEvento().getNombre());

        // UPDATE: modificar mensaje, nombre de usuario y nombre del evento
        saved.setMensaje("Nuevo mensaje de recordatorio");
        saved.getUsuario().setNombre("Luis Actualizado");
        saved.getEvento().setNombre("Seminario Actualizado");
        em.persist(saved);
        em.flush();

        // READ: volver a cargar y comprobar cambios
        Recordatorio updated = em.find(Recordatorio.class, saved.getId_recordatorio());
        assertNotNull(updated);
        assertEquals("Nuevo mensaje de recordatorio", updated.getMensaje());
        assertEquals("Luis Actualizado", updated.getUsuario().getNombre());
        assertEquals("Seminario Actualizado", updated.getEvento().getNombre());

        // DELETE: eliminar el recordatorio
        em.remove(updated);
        em.flush();
        Recordatorio deleted = em.find(Recordatorio.class, saved.getId_recordatorio());
        assertNull(deleted);

        // COMPROBAR que Usuario y Evento siguen existiendo (no cascade delete esperado)
        Usuario persistedUser = null;
        Evento persistedEvent = null;
        try {
            persistedUser = em.getEntityManager()
                    .createQuery("SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class)
                    .setParameter("email", "luis@example.com")
                    .getSingleResult();
        } catch (NoResultException ex) {
            // Usuario no encontrado
        }
        try {
            persistedEvent = em.getEntityManager()
                    .createQuery("SELECT ev FROM Evento ev WHERE ev.nombre_evento = :name", Evento.class)
                    .setParameter("name", "Seminario Actualizado")
                    .getSingleResult();
        } catch (NoResultException ex) {
            // Evento no encontrado
        }

        assertNotNull(persistedUser, "El usuario debe seguir existiendo tras borrar el recordatorio");
        assertNotNull(persistedEvent, "El evento debe seguir existiendo tras borrar el recordatorio");
        assertEquals("Luis Actualizado", persistedUser.getNombre());
        assertEquals("Seminario Actualizado", persistedEvent.getNombre());
    }
}
