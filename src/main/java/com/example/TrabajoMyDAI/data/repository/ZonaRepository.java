package com.example.TrabajoMyDAI.data.repository;

import com.example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.model.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZonaRepository extends JpaRepository<Zona, Long> {

    List<Zona> findByEvento(Evento evento);

    Optional<Zona> findByEventoAndNombre(Evento evento, String nombre);

    @Query("SELECT z FROM Zona z LEFT JOIN FETCH z.tickets WHERE z.evento.id_evento = :eventoId")
    List<Zona> findByEventoIdWithTickets(@Param("eventoId") Long eventoId);
}

