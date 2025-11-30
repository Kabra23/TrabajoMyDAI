package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.services.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
@RequestMapping("/cuenta")
public class AccountController {

    private final UsuarioService usuarioService;

    public AccountController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Get the logged user from session
    private Usuario getUsuarioLogueado(HttpSession session) {
        return (Usuario) session.getAttribute("usuario");
    }

    // Check if user is logged in
    private boolean estaLogueado(HttpSession session) {
        return session.getAttribute("usuario") != null;
    }

    // View profile ("Mi Cuenta")
    @GetMapping("/perfil")
    public String verPerfil(HttpSession session, Model model) {
        if (!estaLogueado(session)) {
            return "redirect:/login";
        }

        Usuario usuario = getUsuarioLogueado(session);
        // Fetch fresh data from database
        Optional<Usuario> usuarioActualizado = usuarioService.encontrarPorId(usuario.getDni());
        if (usuarioActualizado.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuarioActualizado.get());
        model.addAttribute("logueado", true);
        model.addAttribute("esAdmin", usuarioActualizado.get().isAdmin());
        return "mi-cuenta";
    }

    // Show edit profile form
    @GetMapping("/editar")
    public String mostrarFormularioEditarPerfil(HttpSession session, Model model) {
        if (!estaLogueado(session)) {
            return "redirect:/login";
        }

        Usuario usuario = getUsuarioLogueado(session);
        Optional<Usuario> usuarioActualizado = usuarioService.encontrarPorId(usuario.getDni());
        if (usuarioActualizado.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuarioActualizado.get());
        model.addAttribute("logueado", true);
        model.addAttribute("esAdmin", usuarioActualizado.get().isAdmin());
        return "editar-perfil";
    }

    // Update profile
    @PostMapping("/editar")
    public String editarPerfil(@ModelAttribute Usuario usuarioForm,
                                @RequestParam(value = "passwordActual", required = false) String passwordActual,
                                @RequestParam(value = "nuevaPassword", required = false) String nuevaPassword,
                                @RequestParam(value = "confirmarPassword", required = false) String confirmarPassword,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        if (!estaLogueado(session)) {
            return "redirect:/login";
        }

        Usuario usuarioLogueado = getUsuarioLogueado(session);

        try {
            // If changing password, verify current password
            if (nuevaPassword != null && !nuevaPassword.trim().isEmpty()) {
                if (passwordActual == null || passwordActual.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Debes introducir tu contraseña actual para cambiarla");
                    return "redirect:/cuenta/editar";
                }
                
                if (!usuarioService.verificarPassword(usuarioLogueado.getDni(), passwordActual)) {
                    redirectAttributes.addFlashAttribute("error", "La contraseña actual no es correcta");
                    return "redirect:/cuenta/editar";
                }
                
                if (!nuevaPassword.equals(confirmarPassword)) {
                    redirectAttributes.addFlashAttribute("error", "Las contraseñas nuevas no coinciden");
                    return "redirect:/cuenta/editar";
                }
                
                usuarioForm.setPassword(nuevaPassword);
            } else {
                // Don't change password if not provided
                usuarioForm.setPassword(null);
            }

            // User cannot change their own role
            usuarioForm.setRoles(null);

            Usuario usuarioActualizado = usuarioService.actualizarUsuario(usuarioLogueado.getDni(), usuarioForm);
            
            // Update session with new data
            session.setAttribute("usuario", usuarioActualizado);
            session.setAttribute("esAdmin", usuarioActualizado.isAdmin());
            
            redirectAttributes.addFlashAttribute("success", "Perfil actualizado exitosamente");
            return "redirect:/cuenta/perfil";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cuenta/editar";
        }
    }

    // Show delete account confirmation page
    @GetMapping("/eliminar")
    public String mostrarConfirmarEliminar(HttpSession session, Model model) {
        if (!estaLogueado(session)) {
            return "redirect:/login";
        }

        Usuario usuario = getUsuarioLogueado(session);
        model.addAttribute("usuario", usuario);
        model.addAttribute("logueado", true);
        model.addAttribute("esAdmin", usuario.isAdmin());
        return "confirmar-eliminar-cuenta";
    }

    // Delete account
    @PostMapping("/eliminar")
    public String eliminarCuenta(@RequestParam("passwordConfirmacion") String passwordConfirmacion,
                                  HttpSession session, RedirectAttributes redirectAttributes) {
        if (!estaLogueado(session)) {
            return "redirect:/login";
        }

        Usuario usuarioLogueado = getUsuarioLogueado(session);

        // Verify password before deleting
        if (!usuarioService.verificarPassword(usuarioLogueado.getDni(), passwordConfirmacion)) {
            redirectAttributes.addFlashAttribute("error", "La contraseña no es correcta");
            return "redirect:/cuenta/eliminar";
        }

        try {
            usuarioService.eliminarPorId(usuarioLogueado.getDni());
            session.invalidate();
            redirectAttributes.addFlashAttribute("success", "Tu cuenta ha sido eliminada exitosamente");
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cuenta/eliminar";
        }
    }
}
