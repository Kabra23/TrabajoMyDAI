// language: java
package com.example.TrabajoMyDAI.data.services;

import com.example.TrabajoMyDAI.data.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<Usuario> encontrarTodos();
    Optional<Usuario> encontrarPorId(Long id);
    Usuario guardar(Usuario usuario);
    void eliminarPorId(Long id);

    // Additional methods for admin and profile management
    Optional<Usuario> encontrarPorUsername(String username);
    Optional<Usuario> encontrarPorEmail(String email);
    List<Usuario> buscarPorUsernameOEmail(String termino);
    Usuario actualizarUsuario(Long id, Usuario usuarioActualizado);
    boolean verificarPassword(Long id, String password);

    long contarAdmins();
    boolean esUltimoAdmin(Long userId);
    void eliminarUsuarioSeguro(Long idUsuarioAEliminar, Usuario usuarioSolicitante);
}
