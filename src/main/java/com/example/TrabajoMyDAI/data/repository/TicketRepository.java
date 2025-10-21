// TicketRepository.java
package com.example.TrabajoMyDAI.data.repository;

import com.example.TrabajoMyDAI.data.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends CrudRepository<Ticket, Long> {
}
