package com.example.TrabajoMyDAI.data.repository;

import com.example.TrabajoMyDAI.data.model.Jugador;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JugadorRepository extends CrudRepository<Jugador, Long> {

    List<Jugador> findByPosicionIgnoreCase(String posicion);
}