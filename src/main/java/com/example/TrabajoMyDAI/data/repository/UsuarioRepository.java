// language: java
package com.example.TrabajoMyDAI.data.repository;

import com.example.TrabajoMyDAI.data.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
}
