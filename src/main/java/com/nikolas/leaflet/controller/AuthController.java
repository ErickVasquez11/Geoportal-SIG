package com.nikolas.leaflet.controller;

import com.nikolas.leaflet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegisterForm() {
        logger.info("Accediendo a la página de registro");
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        logger.info("Procesando el registro para usuario: {}", username);

        if (!password.equals(confirmPassword)) {
            logger.warn("Las contraseñas no coinciden para el usuario: {}", username);
            model.addAttribute("error", "Las contraseñas no coinciden. Inténtalo de nuevo.");
            return "register";
        }

        try {
            userService.registerNewUser(username, email, password);
            logger.info("Registro exitoso para usuario: {}", username);
            model.addAttribute("success", "Registro exitoso. Ya puedes iniciar sesión.");
            return "register";  // Muestra el formulario de registro con un mensaje de éxito
        } catch (Exception e) {
            logger.error("Error al registrar usuario: {}", e.getMessage());
            model.addAttribute("error", "Ocurrió un error durante el registro. Inténtalo de nuevo.");
            return "register";  // Muestra el formulario con un mensaje de error
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        logger.info("Accediendo a la página de inicio de sesión");
        return "login";
    }
}
