package com.example.TrabajoMyDAI.data.repository;

import com.example.TrabajoMyDAI.data.model. Evento;
import com.example.TrabajoMyDAI.data. model.Ticket;
import com.example.TrabajoMyDAI.data.model.Usuario;
import org.springframework.data. jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByUsuario(Usuario usuario);

    // Nuevo método para verificar si un asiento ya está ocupado en un evento
    Optional<Ticket> findByEventoAndAsiento(Evento evento, Long asiento);
}