package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.data.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// Esta clase se ejecuta "en segundo plano" cada vez que se carga una vista
// La idea es no repetir en cada controller el model.addAttribute("logueado"...), etc.
//Dejamos los atributos "logueado" y "esAdmin" en algunos controllers, ya que no rompen nada
@ControllerAdvice(annotations = Controller.class)
public class SessionFlagsAdvice {

    // Este método se corre antes de renderizar cualquier plantilla Thymeleaf
    // (es decir, antes de devolver un HTML desde un controller)
    @ModelAttribute
    public void addSessionFlags(Model model, HttpSession session) {

        // Tomamos el usuario guardado en la sesión (si existe)
        // Si no hay usuario, esto regresa null
        Usuario u = (Usuario) session.getAttribute("usuario");

        // Variable para Thymeleaf:
        // logueado = true si existe un usuario en sesión
        model.addAttribute("logueado", u != null);

        // Variable para Thymeleaf:
        // esAdmin = true si hay usuario y su rol incluye ADMIN
        model.addAttribute("esAdmin", u != null && u.isAdmin());
    }
}
