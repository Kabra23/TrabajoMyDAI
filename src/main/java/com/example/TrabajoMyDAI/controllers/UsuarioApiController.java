package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.services.UsuarioService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuario")
public class UsuarioApiController {

    private final UsuarioService usuarioService;

    public UsuarioApiController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/saldo")
    public SaldoResponse obtenerSaldo(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return new SaldoResponse(0.0);
        }

        // Recargar desde BD para tener el saldo actualizado
        Optional<Usuario> usuarioActualizado = usuarioService.encontrarPorId(usuario.getDni());
        if (usuarioActualizado.isPresent()) {
            return new SaldoResponse(usuarioActualizado.get().getSaldo());
        }

        return new SaldoResponse(usuario.getSaldo());
    }

    // Clase para la respuesta JSON
    public static class SaldoResponse {
        private Double saldo;

        public SaldoResponse(Double saldo) {
            this.saldo = saldo != null ? saldo : 0.0;
        }

        public Double getSaldo() {
            return saldo;
        }

        public void setSaldo(Double saldo) {
            this.saldo = saldo;
        }
    }
}

