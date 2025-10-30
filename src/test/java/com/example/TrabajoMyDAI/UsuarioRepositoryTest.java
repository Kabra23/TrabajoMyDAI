package com.example.TrabajoMyDAI;

import com.example.TrabajoMyDAI.data.model.Ticket;
import com.example.TrabajoMyDAI.data.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UsuarioRepositoryTest {
    @Autowired
    private TestEntityManager em;

    @Test
    void crudUsuarioYCascadeTickets() {
        Usuario u = new Usuario();
        u.setNombre("Juan");
        u.setEmail("juan@example.com");

        Ticket t1 = new Ticket();
        t1.setPrecio(10.0);
        t1.setAsiento(1L);
        t1.setUsuario(u);

        Ticket t2 = new Ticket();
        t2.setPrecio(12.0);
        t2.setAsiento(2L);
        t2.setUsuario(u);

        u.getTickets().add(t1);
        u.getTickets().add(t2);

        Usuario saved = em.persistFlushFind(u);
        assertNotNull(saved.getDni());
        assertEquals(2, saved.getTickets().size());

        em.remove(saved);
        em.flush();

        Long count = (Long) em.getEntityManager()
                .createQuery("SELECT COUNT(t) FROM Ticket t")
                .getSingleResult();
        assertEquals(0L, count);
    }
}

