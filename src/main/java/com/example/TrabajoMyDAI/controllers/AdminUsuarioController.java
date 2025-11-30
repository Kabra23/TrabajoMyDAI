package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.services.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    private boolean esAdmin(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        return usuario != null && usuario.isAdmin();
    }

    private Usuario getUsuarioLogueado(HttpSession session) {
        return (Usuario) session.getAttribute("usuario");
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(@RequestParam(value = "buscar", required = false) String buscar,
                                 HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        List<Usuario> usuarios;
        if (buscar != null && !buscar.trim().isEmpty()) {
            usuarios = usuarioService.buscarPorUsernameOEmail(buscar);
            model.addAttribute("terminoBusqueda", buscar);
        } else {
            usuarios = usuarioService.encontrarTodos();
        }

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("esAdmin", true);
        model.addAttribute("logueado", true);
        model.addAttribute("usuarioLogueado", getUsuarioLogueado(session));
        return "admin-users";
    }

    @GetMapping("/usuarios/crear")
    public String mostrarFormularioCrearUsuario(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        model.addAttribute("usuario", new Usuario());
        model.addAttribute("esAdmin", true);
        model.addAttribute("logueado", true);
        model.addAttribute("modoEdicion", false);
        return "admin-user-form";
    }

    @PostMapping("/usuarios/crear")
    public String crearUsuario(@ModelAttribute Usuario usuario, HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        try {
            if (usuarioService.encontrarPorUsername(usuario.getUsername()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya existe");
                return "redirect:/admin/usuarios/crear";
            }

            if (usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()
                    && usuarioService.encontrarPorEmail(usuario.getEmail()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "El email ya está registrado");
                return "redirect:/admin/usuarios/crear";
            }

            if (usuario.getRoles() == null || usuario.getRoles().trim().isEmpty()) {
                usuario.setRoles("USER");
            }

            usuarioService.guardar(usuario);
            redirectAttributes.addFlashAttribute("success", "Usuario creado exitosamente");
            return "redirect:/admin/usuarios";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/usuarios/crear";
        }
    }

    @GetMapping("/usuarios/editar/{id}")
    public String mostrarFormularioEditarUsuario(@PathVariable Long id, HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        Optional<Usuario> usuarioOpt = usuarioService.encontrarPorId(id);
        if (usuarioOpt.isEmpty()) {
            return "redirect:/admin/usuarios?error=Usuario no encontrado";
        }

        model.addAttribute("usuario", usuarioOpt.get());
        model.addAttribute("esAdmin", true);
        model.addAttribute("logueado", true);
        model.addAttribute("modoEdicion", true);
        return "admin-user-form";
    }

    @PostMapping("/usuarios/editar/{id}")
    public String editarUsuario(@PathVariable Long id, @ModelAttribute Usuario usuario,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        try {
            usuarioService.actualizarUsuario(id, usuario);
            redirectAttributes.addFlashAttribute("success", "Usuario actualizado exitosamente");
            return "redirect:/admin/usuarios";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/usuarios/editar/" + id;
        }
    }

    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        Usuario usuarioLogueado = getUsuarioLogueado(session);

        if (usuarioLogueado != null && usuarioLogueado.getDni().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "No puedes eliminar tu propia cuenta desde el panel de administración");
            return "redirect:/admin/usuarios";
        }

        try {
            usuarioService.eliminarPorId(id);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado exitosamente");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/usuarios";
    }
}
