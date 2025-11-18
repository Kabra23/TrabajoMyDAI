package com.example.TrabajoMyDAI.data.services;

import com.example.TrabajoMyDAI.data.repository.UsuarioRepository;
import com.example.TrabajoMyDAI.data.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Usuario> encontrarTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    public Optional<Usuario> encontrarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido: " + id);
        }
        return usuarioRepository.findById(id);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío");
        }
        if (usuario.getUsername().length() < 2) {
            throw new IllegalArgumentException("El nombre de usuario debe tener al menos 2 caracteres");
        }
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (!usuario.getEmail().contains("@")) {
            throw new IllegalArgumentException("Formato de email inválido");
        }
        if (usuario.getPassword() == null || usuario.getPassword().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
        if (usuario.getRoles() == null || usuario.getRoles().trim().isEmpty()) {
            usuario.setRoles("USER");
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    public void eliminarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido: " + id);
        }
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario con ID " + id + " no encontrado");
        }
        usuarioRepository.deleteById(id);
    }
}
