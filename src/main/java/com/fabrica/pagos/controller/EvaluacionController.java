package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.EvaluacionDesempeno;
import com.fabrica.pagos.repository.EmpleadoRepository;
import com.fabrica.pagos.repository.EvaluacionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/evaluaciones")
public class EvaluacionController {

    private final EvaluacionRepository evaluacionRepository;
    private final EmpleadoRepository empleadoRepository;

    public EvaluacionController(EvaluacionRepository evaluacionRepository,
                                EmpleadoRepository empleadoRepository) {
        this.evaluacionRepository = evaluacionRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Long empleadoId, Model model) {
        if (empleadoId != null) {
            model.addAttribute("evaluaciones", evaluacionRepository.findByEmpleadoIdOrderByFechaEvaluacionDesc(empleadoId));
        } else {
            model.addAttribute("evaluaciones", evaluacionRepository.findAllByOrderByFechaEvaluacionDesc());
        }
        model.addAttribute("empleados", empleadoRepository.findAllByOrderByCodigoAsc());
        model.addAttribute("empleadoId", empleadoId);
        return "evaluaciones/listar";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        model.addAttribute("evaluacion", new EvaluacionDesempeno());
        model.addAttribute("modo", "nuevo");
        model.addAttribute("empleados", empleadoRepository.findByActivoTrueOrderByCodigoAsc());
        return "evaluaciones/form";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("evaluacion", evaluacionRepository.findById(id).orElseThrow());
        model.addAttribute("modo", "editar");
        model.addAttribute("empleados", empleadoRepository.findByActivoTrueOrderByCodigoAsc());
        return "evaluaciones/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@ModelAttribute EvaluacionDesempeno evaluacion, RedirectAttributes ra) {
        if (evaluacion.getPuntaje() == null || evaluacion.getPuntaje() < 0 || evaluacion.getPuntaje() > 100) {
            ra.addFlashAttribute("mensajeError", "El puntaje debe estar entre 0 y 100");
            return "redirect:/evaluaciones/nuevo";
        }
        evaluacion.setCalificacion(evaluacion.getCalificacionAutomatica());
        evaluacionRepository.save(evaluacion);
        ra.addFlashAttribute("mensajeExito", "Evaluación guardada correctamente (" + evaluacion.getCalificacion() + ")");
        return "redirect:/evaluaciones";
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        evaluacionRepository.deleteById(id);
        ra.addFlashAttribute("mensajeExito", "Evaluación eliminada");
        return "redirect:/evaluaciones";
    }
}
