package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.Empleado;
import com.fabrica.pagos.model.Liquidacion;
import com.fabrica.pagos.repository.EmpleadoRepository;
import com.fabrica.pagos.repository.LiquidacionRepository;
import com.fabrica.pagos.service.LiquidacionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/liquidaciones")
public class LiquidacionController {

    private final LiquidacionRepository liquidacionRepository;
    private final EmpleadoRepository empleadoRepository;
    private final LiquidacionService liquidacionService;

    public LiquidacionController(LiquidacionRepository liquidacionRepository,
                                 EmpleadoRepository empleadoRepository,
                                 LiquidacionService liquidacionService) {
        this.liquidacionRepository = liquidacionRepository;
        this.empleadoRepository = empleadoRepository;
        this.liquidacionService = liquidacionService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("liquidaciones", liquidacionRepository.findAllByOrderByFechaSalidaDesc());
        return "liquidaciones/listar";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        model.addAttribute("liquidacion", new Liquidacion());
        model.addAttribute("empleados", empleadoRepository.findAllByOrderByCodigoAsc());
        return "liquidaciones/form";
    }

    @PostMapping("/generar")
    @PreAuthorize("hasRole('ADMIN')")
    public String generar(@RequestParam Long empleadoId,
                          @RequestParam LocalDate fechaSalida,
                          @RequestParam String motivo,
                          @RequestParam(required = false) BigDecimal otros,
                          @RequestParam(required = false) String observacion,
                          RedirectAttributes ra) {
        Empleado empleado = empleadoRepository.findById(empleadoId).orElseThrow();
        if (fechaSalida == null || fechaSalida.isBefore(empleado.getFechaContratacion())) {
            ra.addFlashAttribute("mensajeError", "La fecha de salida no puede ser anterior a la fecha de contratación");
            return "redirect:/liquidaciones/nuevo";
        }
        Liquidacion liquidacion = liquidacionService.calcular(empleado, fechaSalida, motivo, otros, observacion);
        liquidacionRepository.save(liquidacion);
        if (Boolean.TRUE.equals(empleado.getActivo())) {
            empleado.setActivo(false);
            empleadoRepository.save(empleado);
        }
        ra.addFlashAttribute("mensajeExito",
                "Liquidación generada por C$ " + liquidacion.getTotal() + " para " + empleado.getNombreCompleto());
        return "redirect:/liquidaciones";
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("liquidacion", liquidacionRepository.findById(id).orElseThrow());
        return "liquidaciones/detalle";
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        liquidacionRepository.deleteById(id);
        ra.addFlashAttribute("mensajeExito", "Liquidación eliminada");
        return "redirect:/liquidaciones";
    }
}
