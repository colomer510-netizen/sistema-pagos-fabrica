package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.PermisoVacacion;
import com.fabrica.pagos.repository.EmpleadoRepository;
import com.fabrica.pagos.repository.PermisoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.temporal.ChronoUnit;

@Controller
@RequestMapping("/permisos")
public class PermisoController {

    private final PermisoRepository permisoRepository;
    private final EmpleadoRepository empleadoRepository;

    public PermisoController(PermisoRepository permisoRepository, EmpleadoRepository empleadoRepository) {
        this.permisoRepository = permisoRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Long empleadoId, Model model) {
        if (empleadoId != null) {
            model.addAttribute("permisos", permisoRepository.findByEmpleadoIdOrderByFechaInicioDesc(empleadoId));
        } else {
            model.addAttribute("permisos", permisoRepository.findAllByOrderByFechaInicioDesc());
        }
        model.addAttribute("empleados", empleadoRepository.findByActivoTrueOrderByCodigoAsc());
        model.addAttribute("empleadoId", empleadoId);
        return "permisos/listar";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        model.addAttribute("permiso", new PermisoVacacion());
        model.addAttribute("modo", "nuevo");
        model.addAttribute("empleados", empleadoRepository.findByActivoTrueOrderByCodigoAsc());
        return "permisos/form";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("permiso", permisoRepository.findById(id).orElseThrow());
        model.addAttribute("modo", "editar");
        model.addAttribute("empleados", empleadoRepository.findByActivoTrueOrderByCodigoAsc());
        return "permisos/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@ModelAttribute PermisoVacacion permiso, RedirectAttributes ra) {
        if (permiso.getFechaInicio() == null || permiso.getFechaFin() == null) {
            ra.addFlashAttribute("mensajeError", "Las fechas son obligatorias");
            return "redirect:/permisos/nuevo";
        }
        if (permiso.getFechaFin().isBefore(permiso.getFechaInicio())) {
            ra.addFlashAttribute("mensajeError", "La fecha de fin no puede ser anterior a la de inicio");
            return "redirect:/permisos/nuevo";
        }
        permiso.setDias((int) ChronoUnit.DAYS.between(permiso.getFechaInicio(), permiso.getFechaFin()) + 1);
        if (permiso.getEstado() == null || permiso.getEstado().isBlank()) {
            permiso.setEstado("PENDIENTE");
        }
        permisoRepository.save(permiso);
        ra.addFlashAttribute("mensajeExito", "Permiso/vacación guardado correctamente (" + permiso.getDias() + " día(s))");
        return "redirect:/permisos";
    }

    @PostMapping("/estado/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String estado(@PathVariable Long id, @RequestParam String estado, RedirectAttributes ra) {
        PermisoVacacion permiso = permisoRepository.findById(id).orElseThrow();
        permiso.setEstado(estado);
        permisoRepository.save(permiso);
        ra.addFlashAttribute("mensajeExito", "Estado actualizado a " + estado);
        return "redirect:/permisos";
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        permisoRepository.deleteById(id);
        ra.addFlashAttribute("mensajeExito", "Permiso/vacación eliminado");
        return "redirect:/permisos";
    }
}
