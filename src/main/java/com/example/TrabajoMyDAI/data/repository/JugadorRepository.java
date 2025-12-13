package com.example.TrabajoMyDAI.data.repository;

import com.example.TrabajoMyDAI.data.model.Jugador;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JugadorRepository extends CrudRepository<Jugador, Long> {

    List<Jugador> findByPosicionIgnoreCase(String posicion);

    Optional<Jugador> findByNombreIgnoreCase(String nombre);

    List<Jugador> findByNombreContainingIgnoreCase(String nombre);
}