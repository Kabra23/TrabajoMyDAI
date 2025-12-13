package com.example.TrabajoMyDAI.data.init;

import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(1) // Se ejecuta antes que otros DataLoaders
public class UsuarioDataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDataLoader(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {
        // Crear usuario "andi" si no existe
        Optional<Usuario> andiExistente = usuarioRepository.findByUsername("andi");
        if (andiExistente.isEmpty()) {
            Usuario andi = new Usuario();
            andi.setUsername("andi");
            andi.setPassword("andiandi");
            andi.setNombre("andi");
            andi.setEmail("andi@andi.com");
            andi.setRoles("USER"); // Usuario normal (no admin)
            andi.setSaldo(0.0); // Inicializar saldo en 0
            usuarioRepository.save(andi);
            System.out.println("✅ Usuario creado: username=andi, password=andiandi, email=andi@andi.com, saldo=0.00€");
        } else {
            System.out.println("ℹ️ Usuario 'andi' ya existe en la base de datos");
        }
    }
}

