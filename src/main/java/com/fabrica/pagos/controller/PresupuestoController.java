package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.Presupuesto;
import com.fabrica.pagos.repository.MovimientoCajaRepository;
import com.fabrica.pagos.repository.PresupuestoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/presupuestos")
public class PresupuestoController {

    private final PresupuestoRepository presupuestoRepository;
    private final MovimientoCajaRepository movimientoRepository;

    public PresupuestoController(PresupuestoRepository presupuestoRepository,
                                 MovimientoCajaRepository movimientoRepository) {
        this.presupuestoRepository = presupuestoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String periodo, Model model) {
        List<Presupuesto> presupuestos;
        if (periodo != null && !periodo.isBlank()) {
            presupuestos = new ArrayList<>(presupuestoRepository.findByPeriodoOrderByCategoriaAsc(periodo));
        } else {
            presupuestos = presupuestoRepository.findAllByOrderByPeriodoDesc();
        }
        for (Presupuesto p : presupuestos) {
            YearMonth ym = YearMonth.parse(p.getPeriodo());
            p.setGastoReal(movimientoRepository.sumEgresosPorCategoria(
                    p.getCategoria(), ym.atDay(1), ym.atEndOfMonth()));
        }
        model.addAttribute("presupuestos", presupuestos);
        model.addAttribute("periodoFiltro", periodo);
        return "presupuestos/listar";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setPeriodo(YearMonth.now().toString());
        model.addAttribute("presupuesto", presupuesto);
        return "presupuestos/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@ModelAttribute Presupuesto presupuesto, RedirectAttributes ra) {
        if (presupuesto.getMonto() == null || presupuesto.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("mensajeError", "El monto presupuestado debe ser mayor que cero");
            return "redirect:/presupuestos/nuevo";
        }
        if (presupuestoRepository.findByCategoriaAndPeriodo(presupuesto.getCategoria(), presupuesto.getPeriodo())
                .filter(existente -> !existente.getId().equals(presupuesto.getId()))
                .isPresent()) {
            ra.addFlashAttribute("mensajeError", "Ya existe un presupuesto para esa categoría en ese periodo");
            return "redirect:/presupuestos/nuevo";
        }
        presupuestoRepository.save(presupuesto);
        ra.addFlashAttribute("mensajeExito", "Presupuesto guardado correctamente");
        return "redirect:/presupuestos";
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        presupuestoRepository.deleteById(id);
        ra.addFlashAttribute("mensajeExito", "Presupuesto eliminado");
        return "redirect:/presupuestos";
    }
}
