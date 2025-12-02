package com.example.TrabajoMyDAI.TrabajoMyDAI;

import com.example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.model.Ticket;
import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.repository.EventoRepository;
import com.example.TrabajoMyDAI.data.repository.TicketRepository;
import com.example.TrabajoMyDAI.data.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TicketRepositoryTest {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void testCreateTicket() {
        // Setup related entities
        Usuario u = new Usuario();
        u.setNombre("Ana");
        u.setEmail("ana@example.com");
        Usuario savedUsuario = usuarioRepository.save(u);

        Evento e = new Evento();
        e.setNombre("Feria");
        Evento savedEvento = eventoRepository.save(e);

        Ticket t = new Ticket();
        t.setPrecio(5.0);
        t.setAsiento(10L);
        t.setUsuario(savedUsuario);
        t.setEvento(savedEvento);

        Ticket saved = ticketRepository.save(t);
        assertNotNull(saved.getId_ticket());
        assertEquals(5.0, saved.getPrecio());
        assertEquals(savedUsuario.getDni(), saved.getUsuario().getDni());
        assertEquals(savedEvento.getId(), saved.getEvento().getId());
    }

    @Test
    void testReadTicket() {
        // Setup
        Usuario u = new Usuario();
        u.setNombre("Ana");
        u.setEmail("ana@example.com");
        Usuario savedUsuario = usuarioRepository.save(u);

        Evento e = new Evento();
        e.setNombre("Feria");
        Evento savedEvento = eventoRepository.save(e);

        Ticket t = new Ticket();
        t.setPrecio(5.0);
        t.setAsiento(10L);
        t.setUsuario(savedUsuario);
        t.setEvento(savedEvento);
        Ticket saved = ticketRepository.save(t);
        ticketRepository.flush();

        // Test findById
        Optional<Ticket> found = ticketRepository.findById(saved.getId_ticket());
        assertTrue(found.isPresent());
        assertEquals(saved.getId_ticket(), found.get().getId_ticket());
        assertEquals(5.0, found.get().getPrecio());
    }

    @Test
    void testReadAllTickets() {
        // Setup
        Usuario u = new Usuario();
        u.setNombre("Ana");
        u.setEmail("ana@example.com");
        Usuario savedUsuario = usuarioRepository.save(u);

        Evento e = new Evento();
        e.setNombre("Feria");
        Evento savedEvento = eventoRepository.save(e);

        Ticket t1 = new Ticket();
        t1.setPrecio(5.0);
        t1.setAsiento(10L);
        t1.setUsuario(savedUsuario);
        t1.setEvento(savedEvento);
        ticketRepository.save(t1);

        Ticket t2 = new Ticket();
        t2.setPrecio(6.0);
        t2.setAsiento(11L);
        t2.setUsuario(savedUsuario);
        t2.setEvento(savedEvento);
        ticketRepository.save(t2);

        // Test findAll
        Iterable<Ticket> tickets = ticketRepository.findAll();
        List<Ticket> ticketList = (List<Ticket>) tickets;
        assertTrue(ticketList.size() >= 2);
    }

    @Test
    void testUpdateTicket() {
        // Setup
        Usuario u = new Usuario();
        u.setNombre("Ana");
        u.setEmail("ana@example.com");
        Usuario savedUsuario = usuarioRepository.save(u);

        Evento e = new Evento();
        e.setNombre("Feria");
        Evento savedEvento = eventoRepository.save(e);

        Ticket t = new Ticket();
        t.setPrecio(5.0);
        t.setAsiento(10L);
        t.setUsuario(savedUsuario);
        t.setEvento(savedEvento);
        Ticket saved = ticketRepository.save(t);

        // Update
        saved.setPrecio(7.0);
        Ticket updated = ticketRepository.save(saved);

        // Verify
        assertEquals(7.0, updated.getPrecio());
        Optional<Ticket> found = ticketRepository.findById(saved.getId_ticket());
        assertTrue(found.isPresent());
        assertEquals(7.0, found.get().getPrecio());
    }

    @Test
    void testDeleteTicket() {
        // Setup
        Usuario u = new Usuario();
        u.setNombre("Ana");
        u.setEmail("ana@example.com");
        Usuario savedUsuario = usuarioRepository.save(u);

        Evento e = new Evento();
        e.setNombre("Feria");
        Evento savedEvento = eventoRepository.save(e);

        Ticket t = new Ticket();
        t.setPrecio(5.0);
        t.setAsiento(10L);
        t.setUsuario(savedUsuario);
        t.setEvento(savedEvento);
        Ticket saved = ticketRepository.save(t);

        // Delete
        ticketRepository.deleteById(saved.getId_ticket());

        // Verify
        Optional<Ticket> found = ticketRepository.findById(saved.getId_ticket());
        assertFalse(found.isPresent());
    }

    @Test
    void crudTicketConRelaciones() {
        Usuario u = new Usuario();
        u.setNombre("Ana");
        u.setEmail("ana@example.com");
        Usuario savedUsuario = usuarioRepository.save(u);

        Evento e = new Evento();
        e.setNombre("Feria");
        Evento savedEvento = eventoRepository.save(e);

        Ticket t = new Ticket();
        t.setPrecio(5.0);
        t.setAsiento(10L);
        t.setUsuario(savedUsuario);
        t.setEvento(savedEvento);

        Ticket saved = ticketRepository.save(t);
        ticketRepository.flush();
        Optional<Ticket> foundOpt = ticketRepository.findById(saved.getId_ticket());
        assertTrue(foundOpt.isPresent());
        Ticket found = foundOpt.get();
        assertNotNull(found.getId_ticket());
        assertEquals(savedUsuario.getDni(), found.getUsuario().getDni());
        assertEquals(savedEvento.getId(), found.getEvento().getId());

        // update
        found.setPrecio(6.0);
        ticketRepository.save(found);
        ticketRepository.flush();
        Optional<Ticket> updatedOpt = ticketRepository.findById(found.getId_ticket());
        assertTrue(updatedOpt.isPresent());
        assertEquals(6.0, updatedOpt.get().getPrecio());

        ticketRepository.deleteById(found.getId_ticket());
        ticketRepository.flush();
        Optional<Ticket> deletedOpt = ticketRepository.findById(found.getId_ticket());
        assertFalse(deletedOpt.isPresent());
    }
}
