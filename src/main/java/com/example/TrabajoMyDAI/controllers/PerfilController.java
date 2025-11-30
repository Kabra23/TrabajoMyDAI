package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.services.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
@RequestMapping("/cuenta")
public class PerfilController {

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    private Usuario getUsuarioLogueado(HttpSession session) {
        return (Usuario) session.getAttribute("usuario");
    }

    private boolean estaLogueado(HttpSession session) {
        return getUsuarioLogueado(session) != null;
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(HttpSession session, Model model) {
        if (!estaLogueado(session)) {
            return "redirect:/login";
        }

        Usuario usuario = getUsuarioLogueado(session);
        model.addAttribute("usuario", usuario);
        model.addAttribute("logueado", true);
        model.addAttribute("esAdmin", usuario.isAdmin());
        return "mi-cuenta";
    }

    @GetMapping("/editar")
    public String mostrarFormularioEditar(HttpSession session, Model model) {
        if (!estaLogueado(session)) {
            return "redirect:/login";
        }

        Usuario usuario = getUsuarioLogueado(session);
        model.addAttribute("usuario", usuario);
        model.addAttribute("logueado", true);
        model.addAttribute("esAdmin", usuario.isAdmin());
        return "editar-perfil";
    }

    @PostMapping("/editar")
    public String guardarCambiosPerfil(
            @RequestParam String nombre,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam(required = false) String passwordActual,
            @RequestParam(required = false) String nuevaPassword,
            @RequestParam(required = false) String confirmarPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!estaLogueado(session)) {
            return "redirect:/login";
        }

        Usuario usuarioLogueado = getUsuarioLogueado(session);

        try {
            // Validate password change if requested
            if (nuevaPassword != null && !nuevaPassword.trim().isEmpty()) {
                if (passwordActual == null || passwordActual.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Debes introducir tu contraseña actual para cambiarla");
                    return "redirect:/cuenta/editar";
                }

                if (!usuarioService.verificarPassword(usuarioLogueado.getDni(), passwordActual)) {
                    redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta");
                    return "redirect:/cuenta/editar";
                }

                if (!nuevaPassword.equals(confirmarPassword)) {
                    redirectAttributes.addFlashAttribute("error", "Las contraseñas nuevas no coinciden");
                    return "redirect:/cuenta/editar";
                }

                if (nuevaPassword.length() < 6) {
                    redirectAttributes.addFlashAttribute("error", "La nueva contraseña debe tener al menos 6 caracteres");
                    return "redirect:/cuenta/editar";
                }
            }

            // Create updated user object (keeping the same role)
            Usuario usuarioActualizado = new Usuario();
            usuarioActualizado.setNombre(nombre);
            usuarioActualizado.setUsername(username);
            usuarioActualizado.setEmail(email);
            usuarioActualizado.setRoles(usuarioLogueado.getRoles()); // Keep same role

            if (nuevaPassword != null && !nuevaPassword.trim().isEmpty()) {
                usuarioActualizado.setPassword(nuevaPassword);
            }

            Usuario actualizado = usuarioService.actualizarUsuario(usuarioLogueado.getDni(), usuarioActualizado);

            // Update session with new user data
            session.setAttribute("usuario", actualizado);
            session.setAttribute("esAdmin", actualizado.isAdmin());

            redirectAttributes.addFlashAttribute("success", "Perfil actualizado exitosamente");
            return "redirect:/cuenta/perfil";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cuenta/editar";
        }
    }

    @GetMapping("/eliminar")
    public String mostrarConfirmacionEliminar(HttpSession session, Model model) {
        if (!estaLogueado(session)) {
            return "redirect:/login";
        }

        Usuario usuario = getUsuarioLogueado(session);

        // Admins cannot delete their own account from their profile
        if (usuario.isAdmin()) {
            model.addAttribute("error", "Los administradores no pueden eliminar su propia cuenta");
            model.addAttribute("usuario", usuario);
            model.addAttribute("logueado", true);
            model.addAttribute("esAdmin", true);
            return "mi-cuenta";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("logueado", true);
        model.addAttribute("esAdmin", usuario.isAdmin());
        return "eliminar-cuenta";
    }

    @PostMapping("/eliminar")
    public String eliminarCuenta(
            @RequestParam String passwordConfirmacion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!estaLogueado(session)) {
            return "redirect:/login";
        }

        Usuario usuarioLogueado = getUsuarioLogueado(session);

        // Admins cannot delete their own account
        if (usuarioLogueado.isAdmin()) {
            redirectAttributes.addFlashAttribute("error", "Los administradores no pueden eliminar su propia cuenta");
            return "redirect:/cuenta/perfil";
        }

        // Verify password
        if (!usuarioService.verificarPassword(usuarioLogueado.getDni(), passwordConfirmacion)) {
            redirectAttributes.addFlashAttribute("error", "La contraseña es incorrecta");
            return "redirect:/cuenta/eliminar";
        }

        try {
            usuarioService.eliminarUsuarioSeguro(usuarioLogueado.getDni(), usuarioLogueado);
            session.invalidate();
            return "redirect:/?mensaje=cuenta_eliminada";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cuenta/eliminar";
        }
    }
}