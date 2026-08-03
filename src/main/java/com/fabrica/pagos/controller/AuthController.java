package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.Empresa;
import com.fabrica.pagos.service.EmpresaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final EmpresaService empresaService;

    public AuthController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            switch (error) {
                case "bloqueado" ->
                        model.addAttribute("mensajeError", "Cuenta bloqueada por intentos fallidos. Intenta más tarde.");
                case "inactivo" ->
                        model.addAttribute("mensajeError", "El usuario está inactivo. Contacta al administrador.");
                default -> model.addAttribute("mensajeError", "Usuario o contraseña incorrectos");
            }
        }
        if (logout != null) {
            model.addAttribute("mensajeExito", "Sesión cerrada correctamente");
        }
        Empresa empresa = empresaService.obtener();
        model.addAttribute("empresa", empresa);
        model.addAttribute("titulo", empresa.getNombre());
        return "login";
    }
}