package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data. repository.UsuarioRepository;
import com.example.TrabajoMyDAI.data.services.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation. GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework. web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioRepository usuarioRepository, UsuarioService usuarioService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    // Crear usuario admin al iniciar la aplicación
    @PostConstruct
    public void init() {
        // Reiniciar la secuencia de IDs de Usuario (solo para H2)
        try {
            usuarioRepository.resetSequence();
        } catch (Exception e) {
            // Si falla, continuar sin problemas (puede ser que la tabla aún no exista)
        }

        Optional<Usuario> adminExistente = usuarioRepository.findByUsername("admin");
        if (adminExistente.isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword("admin");
            admin.setNombre("Administrador");
            admin.setEmail("admin@barcaathletic.com");
            admin.setRoles("ADMIN");
            usuarioRepository.save(admin);
            System.out.println("✅ Usuario administrador creado: username=admin, password=admin");
        }
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("usuario") Usuario usuario, HttpSession session, Model model) {
        String username = usuario.getUsername();
        String password = usuario.getPassword();
        Optional<Usuario> optionalUsuario = usuarioRepository. findByUsername(username);
        if (optionalUsuario.isPresent() && optionalUsuario. get().getPassword().equals(password)) {
            Usuario user = optionalUsuario.get();
            session.setAttribute("usuario", user);
            session.setAttribute("esAdmin", user.isAdmin());
            return "redirect:/";
        }
        model.addAttribute("error", "Credenciales inválidas");
        model.addAttribute("usuario", usuario);
        return "login";
    }

    @GetMapping("/registro")
    public String registroPage(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String registro(@ModelAttribute("usuario") Usuario usuario, Model model) {
        try {
            String username = usuario.getUsername();
            if (usuarioRepository.findByUsername(username).isPresent()) {
                model.addAttribute("error", "El usuario ya existe");
                model.addAttribute("usuario", usuario);
                return "registro";
            }
            usuario.setRoles("USER");
            usuarioService.guardar(usuario);
            model. addAttribute("registroExitoso", true);
            model.addAttribute("usuario", new Usuario());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuario", usuario);
        }
        return "registro";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session. invalidate();
        return "redirect:/";
    }


}