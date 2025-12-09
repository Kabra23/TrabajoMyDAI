package com.example.TrabajoMyDAI.TrabajoMyDAI;

import com.example.TrabajoMyDAI.data.model.Ticket;
import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.repository.TicketRepository;
import com.example.TrabajoMyDAI.data.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UsuarioRepositoryTest {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void testCreateUsuario() {
        Usuario u = new Usuario();
        u.setNombre("Juan");
        u.setEmail("juan@example.com");

        Usuario saved = usuarioRepository.save(u);
        assertNotNull(saved.getDni());
        assertEquals("Juan", saved.getNombre());
        assertEquals("juan@example.com", saved.getEmail());
    }

    @Test
    void testReadUsuario() {
        Usuario u = new Usuario();
        u.setNombre("Juan");
        u.setEmail("juan@example.com");
        Usuario saved = usuarioRepository.save(u);
        usuarioRepository.flush();

        Optional<Usuario> found = usuarioRepository.findById(saved.getDni());
        assertTrue(found.isPresent());
        assertEquals(saved.getDni(), found.get().getDni());
        assertEquals("Juan", found.get().getNombre());
    }

    @Test
    void testReadAllUsuarios() {
        Usuario u1 = new Usuario();
        u1.setNombre("Juan");
        u1.setEmail("juan@example.com");
        usuarioRepository.save(u1);

        Usuario u2 = new Usuario();
        u2.setNombre("Ana");
        u2.setEmail("ana@example.com");
        usuarioRepository.save(u2);

        Iterable<Usuario> usuarios = usuarioRepository.findAll();
        List<Usuario> usuarioList = (List<Usuario>) usuarios;
        assertTrue(usuarioList.size() >= 2);
    }

    @Test
    void testUpdateUsuario() {
        Usuario u = new Usuario();
        u.setNombre("Juan");
        u.setEmail("juan@example.com");
        Usuario saved = usuarioRepository.save(u);

        saved.setNombre("Juan Carlos");
        Usuario updated = usuarioRepository.save(saved);

        assertEquals("Juan Carlos", updated.getNombre());
        Optional<Usuario> found = usuarioRepository.findById(saved.getDni());
        assertTrue(found.isPresent());
        assertEquals("Juan Carlos", found.get().getNombre());
    }

    @Test
    void testDeleteUsuario() {
        Usuario u = new Usuario();
        u.setNombre("Juan");
        u.setEmail("juan@example.com");
        Usuario saved = usuarioRepository.save(u);

        usuarioRepository.deleteById(saved.getDni());

        Optional<Usuario> found = usuarioRepository.findById(saved.getDni());
        assertFalse(found.isPresent());
    }

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

        Usuario saved = usuarioRepository.save(u);
        usuarioRepository.flush();
        Optional<Usuario> foundOpt = usuarioRepository.findById(saved.getDni());
        assertTrue(foundOpt.isPresent());
        Usuario found = foundOpt.get();
        assertNotNull(found.getDni());
        assertEquals(2, found.getTickets().size());

        usuarioRepository.deleteById(found.getDni());
        usuarioRepository.flush();

        long count = ticketRepository.count();
        assertEquals(0L, count);
    }
}