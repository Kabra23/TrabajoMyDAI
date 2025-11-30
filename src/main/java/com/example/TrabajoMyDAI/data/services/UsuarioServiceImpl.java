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
        if (usuario.getPassword() == null || usuario.getPassword().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
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

    @Override
    public Optional<Usuario> encontrarPorUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return usuarioRepository.findByUsername(username);
    }

    @Override
    public Optional<Usuario> encontrarPorEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public List<Usuario> buscarPorUsernameOEmail(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return usuarioRepository.findAll();
        }
        return usuarioRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(termino, termino);
    }

    @Override
    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido: " + id);
        }
        
        Usuario usuarioExistente = usuarioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario con ID " + id + " no encontrado"));
        
        // Update fields if provided
        if (usuarioActualizado.getUsername() != null && !usuarioActualizado.getUsername().trim().isEmpty()) {
            if (usuarioActualizado.getUsername().length() < 2) {
                throw new IllegalArgumentException("El nombre de usuario debe tener al menos 2 caracteres");
            }
            // Check if username is already taken by another user
            Optional<Usuario> existingWithUsername = usuarioRepository.findByUsername(usuarioActualizado.getUsername());
            if (existingWithUsername.isPresent() && !existingWithUsername.get().getDni().equals(id)) {
                throw new IllegalArgumentException("El nombre de usuario ya está en uso");
            }
            usuarioExistente.setUsername(usuarioActualizado.getUsername());
        }
        
        if (usuarioActualizado.getNombre() != null) {
            usuarioExistente.setNombre(usuarioActualizado.getNombre());
        }
        
        if (usuarioActualizado.getEmail() != null && !usuarioActualizado.getEmail().trim().isEmpty()) {
            // Check if email is already taken by another user
            Optional<Usuario> existingWithEmail = usuarioRepository.findByEmail(usuarioActualizado.getEmail());
            if (existingWithEmail.isPresent() && !existingWithEmail.get().getDni().equals(id)) {
                throw new IllegalArgumentException("El email ya está en uso");
            }
            usuarioExistente.setEmail(usuarioActualizado.getEmail());
        }
        
        if (usuarioActualizado.getRoles() != null) {
            usuarioExistente.setRoles(usuarioActualizado.getRoles());
        }
        
        // Only update password if a new one is provided
        if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().trim().isEmpty()) {
            if (usuarioActualizado.getPassword().length() < 6) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
            }
            usuarioExistente.setPassword(usuarioActualizado.getPassword());
        }
        
        return usuarioRepository.save(usuarioExistente);
    }

    @Override
    public boolean verificarPassword(Long id, String password) {
        if (id == null || id <= 0 || password == null) {
            return false;
        }
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.isPresent() && usuario.get().getPassword().equals(password);
    }
}
