package com.example.TrabajoMyDAI;

import com.example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.model.Ticket;
import com.example.TrabajoMyDAI.data.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TicketRepositoryTest {
    @Autowired
    private TestEntityManager em;

    @Test
    void crudTicketConRelaciones() {
        Usuario u = new Usuario();
        u.setNombre("Ana");
        u.setEmail("ana@example.com");
        em.persist(u);

        Evento e = new Evento();
        e.setNombre("Feria");
        em.persist(e);

        Ticket t = new Ticket();
        t.setPrecio(5.0);
        t.setAsiento(10L);
        t.setUsuario(u);
        t.setEvento(e);

        Ticket saved = em.persistFlushFind(t);
        assertNotNull(saved.getId_ticket());
        assertEquals(u.getDni(), saved.getUsuario().getDni());
        assertEquals(e.getId(), saved.getEvento().getId());

        // update
        saved.setPrecio(6.0);
        em.persistAndFlush(saved);
        Ticket updated = em.find(Ticket.class, saved.getId_ticket());
        assertEquals(6.0, updated.getPrecio());

        em.remove(updated);
        em.flush();
        Ticket deleted = em.find(Ticket.class, saved.getId_ticket());
        assertNull(deleted);
    }
}

