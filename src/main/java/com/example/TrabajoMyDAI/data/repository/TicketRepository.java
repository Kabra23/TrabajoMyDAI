package com.example.TrabajoMyDAI.data.repository;

import com.example.TrabajoMyDAI.data.model. Evento;
import com.example.TrabajoMyDAI.data. model.Ticket;
import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.model.Zona;
import org.springframework.data. jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByUsuario(Usuario usuario);

    // Método para verificar si un asiento ya está ocupado en un evento (sin considerar zona)
    Optional<Ticket> findByEventoAndAsiento(Evento evento, Long asiento);

    // NUEVO: Método para verificar si un asiento ya está ocupado en una zona específica
    Optional<Ticket> findByZonaAndAsiento(Zona zona, Long asiento);
}