package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.Asistencia;
import com.fabrica.pagos.model.Empleado;
import com.fabrica.pagos.repository.AsistenciaRepository;
import com.fabrica.pagos.repository.EmpleadoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Controller
@RequestMapping("/asistencia")
public class AsistenciaController {

    private final AsistenciaRepository asistenciaRepository;
    private final EmpleadoRepository empleadoRepository;

    public AsistenciaController(AsistenciaRepository asistenciaRepository,
                                EmpleadoRepository empleadoRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) LocalDate fecha, Model model) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicio;
        LocalDate fin;
        if (fecha != null) {
            inicio = fecha.with(TemporalAdjusters.firstDayOfMonth());
            fin = fecha.with(TemporalAdjusters.lastDayOfMonth());
        } else {
            inicio = hoy.with(TemporalAdjusters.firstDayOfMonth());
            fin = hoy.with(TemporalAdjusters.lastDayOfMonth());
        }
        model.addAttribute("registros", asistenciaRepository.findByFechaBetweenOrderByFechaAsc(inicio, fin));
        model.addAttribute("inicio", inicio);
        model.addAttribute("fin", fin);
        return "asistencia/listar";
    }

    @GetMapping("/registrar")
    @PreAuthorize("hasRole('ADMIN')")
    public String registrar(Model model) {
        model.addAttribute("empleados", empleadoRepository.findByActivoTrueOrderByCodigoAsc());
        model.addAttribute("asistencia", new Asistencia());
        model.addAttribute("hoy", LocalDate.now());
        return "asistencia/registrar";
    }

    @PostMapping("/registrar")
    @PreAuthorize("hasRole('ADMIN')")
    public String registrarAsistencia(@RequestParam Long empleadoId,
                                      @RequestParam LocalDate fecha,
                                      @RequestParam Integer horasTrabajadas,
                                      @RequestParam(required = false) String estado,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        Empleado empleado = empleadoRepository.findById(empleadoId).orElseThrow();
        if (asistenciaRepository.findByEmpleadoIdAndFecha(empleadoId, fecha).isPresent()) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Ya existe un registro de asistencia para " + empleado.getNombreCompleto() + " el " + fecha);
            return "redirect:/asistencia/registrar";
        }

        Asistencia asistencia = new Asistencia();
        asistencia.setEmpleado(empleado);
        asistencia.setFecha(fecha);
        asistencia.setHorasTrabajadas(horasTrabajadas);
        asistencia.setEstado(estado == null || estado.isBlank() ? "P" : estado);
        asistenciaRepository.save(asistencia);
        redirectAttributes.addFlashAttribute("mensajeExito",
                "Asistencia registrada para " + empleado.getNombreCompleto());
        return "redirect:/asistencia";
    }

    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        asistenciaRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Registro de asistencia eliminado");
        return "redirect:/asistencia";
    }

    @ModelAttribute("estados")
    public List<String> estados() {
        return List.of("P", "E", "V", "F");
    }
}