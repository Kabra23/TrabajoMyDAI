package com.example.TrabajoMyDAI;

import com.example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.model.Recordatorio;
import com.example.TrabajoMyDAI.data.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class RecordatorioRepositoryTest {
    @Autowired
    private TestEntityManager em;

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
        r.setFecha("2025-11-01");

        Recordatorio saved = em.persistFlushFind(r);
        assertNotNull(saved.getId_recordatorio());
        assertEquals("Recordar inscripción", saved.getMensaje());

        em.remove(saved);
        em.flush();
        Recordatorio deleted = em.find(Recordatorio.class, saved.getId_recordatorio());
        assertNull(deleted);
    }
}