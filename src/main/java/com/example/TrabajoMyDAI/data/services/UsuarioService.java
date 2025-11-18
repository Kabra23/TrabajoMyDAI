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
}
