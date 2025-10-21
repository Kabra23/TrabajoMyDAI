package com.example.TrabajoMyDAI.data.repository;

import com.example.TrabajoMyDAI.data.model.Usuario;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UsuarioRepository extends CrudRepository<Usuario,Long>{


}


