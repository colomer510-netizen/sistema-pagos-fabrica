package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.DeduccionConfig;
import com.fabrica.pagos.repository.DeduccionConfigRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/deducciones")
public class DeduccionConfigController {

    private final DeduccionConfigRepository deduccionConfigRepository;

    public DeduccionConfigController(DeduccionConfigRepository deduccionConfigRepository) {
        this.deduccionConfigRepository = deduccionConfigRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("deducciones", deduccionConfigRepository.findAllByOrderByNombreAsc());
        return "deducciones/listar";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        model.addAttribute("deduccion", new DeduccionConfig());
        model.addAttribute("modo", "nuevo");
        return "deducciones/form";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("deduccion", deduccionConfigRepository.findById(id).orElseThrow());
        model.addAttribute("modo", "editar");
        return "deducciones/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@ModelAttribute DeduccionConfig deduccion, RedirectAttributes ra) {
        try {
            deduccionConfigRepository.save(deduccion);
            ra.addFlashAttribute("mensajeExito", "Deducción guardada correctamente");
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("mensajeError", "Ya existe una deducción con ese nombre");
        }
        return "redirect:/deducciones";
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        deduccionConfigRepository.deleteById(id);
        ra.addFlashAttribute("mensajeExito", "Deducción eliminada");
        return "redirect:/deducciones";
    }

    @PostMapping("/estado/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes ra) {
        DeduccionConfig d = deduccionConfigRepository.findById(id).orElseThrow();
        d.setActiva(!Boolean.TRUE.equals(d.getActiva()));
        deduccionConfigRepository.save(d);
        ra.addFlashAttribute("mensajeExito", "Estado actualizado");
        return "redirect:/deducciones";
    }
}
