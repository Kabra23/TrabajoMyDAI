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
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent() && usuarioOpt.get().isAdmin()) {
            if (contarAdmins() <= 1) {
                throw new IllegalArgumentException("No se puede eliminar el último administrador del sistema");
            }
        }
        if (usuarioOpt. isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        Usuario usuario = usuarioOpt.get();

        if (usuario. isAdmin()) {
            long cantidadAdmins = usuarioRepository.findAll().stream()
                    .filter(Usuario::isAdmin)
                    .count();

            if (cantidadAdmins <= 1) {
                throw new IllegalArgumentException("No se puede eliminar el último administrador del sistema");
            }
        }

        usuarioRepository. deleteById(id);
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
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);
        if (usuarioOptional.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }


        Usuario usuarioExistente = usuarioOptional.get();


        if (usuarioActualizado.getRoles() != null) {
            boolean eraAdmin = usuarioExistente.isAdmin();
            boolean seraAdmin = usuarioActualizado.getRoles().contains("ADMIN");

            // If user was admin and will not be admin anymore
            if (eraAdmin && !seraAdmin) {
                if (contarAdmins() <= 1) {
                    throw new IllegalArgumentException("No se puede quitar el rol de administrador al último admin del sistema");
                }
            }

            // Si el usuario se convierte en admin, resetear el saldo a 0
            if (!eraAdmin && seraAdmin) {
                usuarioExistente.setSaldo(0.0);
            }
        }

        if (usuarioExistente.isAdmin() && usuarioActualizado.getRoles() != null
                && ! usuarioActualizado.getRoles().contains("ADMIN")) {

            long cantidadAdmins = usuarioRepository.findAll().stream()
                    .filter(Usuario::isAdmin)
                    .count();

            if (cantidadAdmins <= 1) {
                throw new IllegalArgumentException("No se puede quitar el rol de administrador al último admin del sistema");
            }
        }

        // Resto del código de actualización...
        if (usuarioActualizado.getUsername() != null && ! usuarioActualizado.getUsername().equals(usuarioExistente.getUsername())) {
            if (usuarioRepository.findByUsername(usuarioActualizado.getUsername()).isPresent()) {
                throw new IllegalArgumentException("El nombre de usuario ya está en uso");
            }
            usuarioExistente.setUsername(usuarioActualizado.getUsername());
        }

        if (usuarioActualizado.getNombre() != null) {
            usuarioExistente.setNombre(usuarioActualizado.getNombre());
        }

        if (usuarioActualizado.getEmail() != null && !usuarioActualizado.getEmail().trim().isEmpty()) {
            Optional<Usuario> existingWithEmail = usuarioRepository.findByEmail(usuarioActualizado. getEmail());
            if (existingWithEmail.isPresent() && !existingWithEmail.get().getDni().equals(id)) {
                throw new IllegalArgumentException("El email ya está en uso");
            }
            usuarioExistente.setEmail(usuarioActualizado.getEmail());
        }

        if (usuarioActualizado.getRoles() != null) {
            usuarioExistente.setRoles(usuarioActualizado. getRoles());
        }

        if (usuarioActualizado. getPassword() != null && !usuarioActualizado.getPassword(). trim().isEmpty()) {
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

    @Override
    public long contarAdmins() {
        return usuarioRepository.countAdmins();
    }

    @Override
    public boolean esUltimoAdmin(Long userId) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(userId);
        if (usuarioOpt.isEmpty() || !usuarioOpt.get().isAdmin()) {
            return false;
        }
        return contarAdmins() <= 1;
    }

    @Override
    public void eliminarUsuarioSeguro(Long idUsuarioAEliminar, Usuario usuarioSolicitante) {
        if (idUsuarioAEliminar == null || idUsuarioAEliminar <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido: " + idUsuarioAEliminar);
        }

        Optional<Usuario> usuarioAEliminarOpt = usuarioRepository.findById(idUsuarioAEliminar);
        if (usuarioAEliminarOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario con ID " + idUsuarioAEliminar + " no encontrado");
        }

        Usuario usuarioAEliminar = usuarioAEliminarOpt.get();

        // Rule: Normal users cannot delete admin accounts
        if (usuarioSolicitante != null && !usuarioSolicitante.isAdmin() && usuarioAEliminar.isAdmin()) {
            throw new IllegalArgumentException("No tienes permisos para eliminar cuentas de administrador");
        }

        // Rule: Cannot delete last admin
        if (usuarioAEliminar.isAdmin() && contarAdmins() <= 1) {
            throw new IllegalArgumentException("No se puede eliminar el último administrador del sistema");
        }

        usuarioRepository.deleteById(idUsuarioAEliminar);

        // Reorganizar IDs después de eliminar
        reorganizarIds();
    }

    @Override
    public void reorganizarIds() {
        List<Usuario> todosLosUsuarios = usuarioRepository.findAll();

        if (todosLosUsuarios.isEmpty()) {
            // Si no hay usuarios, reiniciar la secuencia a 1
            usuarioRepository.resetSequence();
            return;
        }

        // Ordenar usuarios por ID actual (menor a mayor)
        todosLosUsuarios.sort(java.util.Comparator.comparingLong(Usuario::getDni));

        // Reorganizar IDs de forma compacta: 1, 2, 3, 4, ...
        // Esto elimina "huecos" en la secuencia
        for (int i = 0; i < todosLosUsuarios.size(); i++) {
            Usuario usuario = todosLosUsuarios.get(i);
            long idActual = usuario.getDni();
            long nuevoId = i + 1;

            // Solo actualizar si el ID debe cambiar
            if (idActual != nuevoId) {
                // Usar un ID temporal grande para evitar conflictos
                long idTemporal = 10000L + idActual;

                // Paso 1: Mover a ID temporal
                usuarioRepository.actualizarId(idActual, idTemporal);
                usuarioRepository.flush();

                // Actualizar el objeto en memoria
                usuario.setDni(idTemporal);
            }
        }

        // Paso 2: Ahora asignar los IDs finales
        for (int i = 0; i < todosLosUsuarios.size(); i++) {
            Usuario usuario = todosLosUsuarios.get(i);
            long idActual = usuario.getDni();
            long nuevoId = i + 1;

            if (idActual != nuevoId) {
                usuarioRepository.actualizarId(idActual, nuevoId);
                usuarioRepository.flush();
                usuario.setDni(nuevoId);
            }
        }

        // Paso 3: Reiniciar la secuencia al siguiente ID disponible
        long proximoId = todosLosUsuarios.size() + 1;
        usuarioRepository.resetSequenceToNextId(proximoId);
    }

    @Override
    public void agregarSaldo(Long usuarioId, Double cantidad) {
        if (usuarioId == null || cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("ID de usuario y cantidad deben ser válidos y positivos");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();
        if (usuario.isAdmin()) {
            throw new IllegalArgumentException("Los administradores no pueden tener saldo");
        }

        usuario.agregarSaldo(cantidad);
        usuarioRepository.save(usuario);
    }

    @Override
    public boolean descontarSaldo(Long usuarioId, Double cantidad) {
        if (usuarioId == null || cantidad == null || cantidad <= 0) {
            return false;
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOpt.get();
        if (usuario.isAdmin()) {
            return false;
        }

        if (usuario.descontarSaldo(cantidad)) {
            usuarioRepository.save(usuario);
            return true;
        }
        return false;
    }

    @Override
    public Double consultarSaldo(Long usuarioId) {
        if (usuarioId == null) {
            return 0.0;
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return 0.0;
        }

        Usuario usuario = usuarioOpt.get();
        return usuario.isAdmin() ? 0.0 : usuario.getSaldo();
    }
}

