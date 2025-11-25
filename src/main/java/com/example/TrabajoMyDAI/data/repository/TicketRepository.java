// TicketRepository.java
package com.example.TrabajoMyDAI.data.repository;

import com.example.TrabajoMyDAI.data.model.Ticket;
import com.example.TrabajoMyDAI.data.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUsuario(Usuario usuario);
}
