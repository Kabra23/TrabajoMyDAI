package com.example.TrabajoMyDAI.data.repository;

import com.example.TrabajoMyDAI.data.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.roles LIKE '%ADMIN%'")
    long countAdmins();
}