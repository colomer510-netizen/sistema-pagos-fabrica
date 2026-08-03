package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.Usuario;
import com.fabrica.pagos.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAllByOrderByUsernameAsc());
        return "usuarios/listar";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("modo", "nuevo");
        return "usuarios/form";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioRepository.findById(id).orElseThrow());
        model.addAttribute("modo", "editar");
        return "usuarios/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@ModelAttribute Usuario usuario,
                          @RequestParam(required = false) String password,
                          RedirectAttributes ra) {
        try {
            if (usuario.getId() != null) {
                Usuario existente = usuarioRepository.findById(usuario.getId()).orElseThrow();
                existente.setUsername(usuario.getUsername());
                existente.setNombre(usuario.getNombre());
                existente.setRol(usuario.getRol());
                existente.setActivo(usuario.getActivo());
                existente.setCambiarPassword(usuario.getCambiarPassword());
                if (password != null && !password.isBlank()) {
                    existente.setPassword(passwordEncoder.encode(password));
                    existente.setCambiarPassword(true);
                }
                usuarioRepository.save(existente);
            } else {
                if (password == null || password.isBlank()) {
                    ra.addFlashAttribute("mensajeError", "La contraseña es obligatoria para un usuario nuevo");
                    return "redirect:/usuarios/nuevo";
                }
                usuario.setPassword(passwordEncoder.encode(password));
                usuarioRepository.save(usuario);
            }
            ra.addFlashAttribute("mensajeExito", "Usuario guardado correctamente");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("mensajeError", "Ya existe un usuario con ese nombre");
            return "redirect:/usuarios";
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        if (auth != null && auth.getName().equals(usuario.getUsername())) {
            ra.addFlashAttribute("mensajeError", "No puedes eliminar tu propio usuario");
            return "redirect:/usuarios";
        }
        usuarioRepository.deleteById(id);
        ra.addFlashAttribute("mensajeExito", "Usuario eliminado");
        return "redirect:/usuarios";
    }

    @PostMapping("/estado/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String cambiarEstado(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        if (auth != null && auth.getName().equals(usuario.getUsername())) {
            ra.addFlashAttribute("mensajeError", "No puedes cambiar tu propio estado");
            return "redirect:/usuarios";
        }
        usuario.setActivo(!Boolean.TRUE.equals(usuario.getActivo()));
        usuarioRepository.save(usuario);
        ra.addFlashAttribute("mensajeExito", "Estado actualizado");
        return "redirect:/usuarios";
    }

    @PostMapping("/desbloquear/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String desbloquear(@PathVariable Long id, RedirectAttributes ra) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        usuario.setIntentosFallidos(0);
        usuario.setFechaBloqueo(null);
        usuarioRepository.save(usuario);
        ra.addFlashAttribute("mensajeExito", "Cuenta desbloqueada correctamente");
        return "redirect:/usuarios";
    }
}
