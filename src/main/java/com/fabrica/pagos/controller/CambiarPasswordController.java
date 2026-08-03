package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.Usuario;
import com.fabrica.pagos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class CambiarPasswordController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.password-expiracion-dias:0}")
    private long expiracionDias;

    public CambiarPasswordController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/cambiar-password")
    public String form(Model model) {
        model.addAttribute("titulo", "Cambiar contraseña");
        return "cambiar-password";
    }

    @PostMapping("/cambiar-password")
    public String cambiar(@RequestParam String passwordActual,
                          @RequestParam String passwordNueva,
                          @RequestParam String confirmacion,
                          Authentication authentication,
                          RedirectAttributes ra) {
        Usuario usuario = usuarioRepository.findByUsername(authentication.getName()).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            ra.addFlashAttribute("mensajeError", "La contraseña actual es incorrecta");
            return "redirect:/cambiar-password";
        }
        if (passwordNueva.length() < 8) {
            ra.addFlashAttribute("mensajeError", "La nueva contraseña debe tener al menos 8 caracteres");
            return "redirect:/cambiar-password";
        }
        if (!passwordNueva.equals(confirmacion)) {
            ra.addFlashAttribute("mensajeError", "La confirmación no coincide con la nueva contraseña");
            return "redirect:/cambiar-password";
        }
        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuario.setCambiarPassword(false);
        if (expiracionDias > 0) {
            usuario.setFechaExpiracionPassword(LocalDate.now().plusDays(expiracionDias));
        } else {
            usuario.setFechaExpiracionPassword(null);
        }
        usuarioRepository.save(usuario);
        ra.addFlashAttribute("mensajeExito", "Contraseña actualizada correctamente");
        return "redirect:/dashboard";
    }
}
