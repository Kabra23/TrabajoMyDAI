// EventoUsuarioRepository.java
package com.example.TrabajoMyDAI.repository;

import com.example.TrabajoMyDAI.data.model.Evento_Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoUsuarioRepository extends CrudRepository<Evento_Usuario, Long> {
}
