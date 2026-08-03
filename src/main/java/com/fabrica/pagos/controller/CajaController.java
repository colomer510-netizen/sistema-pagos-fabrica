package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.MovimientoCaja;
import com.fabrica.pagos.repository.MovimientoCajaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Controller
@RequestMapping("/caja")
public class CajaController {

    private final MovimientoCajaRepository movimientoRepository;

    public CajaController(MovimientoCajaRepository movimientoRepository) {
        this.movimientoRepository = movimientoRepository;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String mes, Model model) {
        YearMonth ym = (mes == null || mes.isBlank()) ? YearMonth.now() : YearMonth.parse(mes);
        LocalDate inicio = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();
        BigDecimal ingresos = movimientoRepository.sumByTipoBetween("INGRESO", inicio, fin);
        BigDecimal egresos = movimientoRepository.sumByTipoBetween("EGRESO", inicio, fin);
        model.addAttribute("mes", ym.toString());
        model.addAttribute("movimientos", movimientoRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin));
        model.addAttribute("ingresos", ingresos);
        model.addAttribute("egresos", egresos);
        model.addAttribute("saldoMes", ingresos.subtract(egresos));
        model.addAttribute("saldoAcumulado", movimientoRepository.saldoAcumulado());
        return "caja/listar";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setFecha(LocalDate.now());
        model.addAttribute("movimiento", movimiento);
        return "caja/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@ModelAttribute MovimientoCaja movimiento, RedirectAttributes ra) {
        if (movimiento.getMonto() == null || movimiento.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("mensajeError", "El monto debe ser mayor que cero");
            return "redirect:/caja/nuevo";
        }
        movimientoRepository.save(movimiento);
        ra.addFlashAttribute("mensajeExito",
                "Movimiento de " + movimiento.getTipo() + " registrado: C$ " + movimiento.getMonto());
        return "redirect:/caja";
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        movimientoRepository.deleteById(id);
        ra.addFlashAttribute("mensajeExito", "Movimiento eliminado");
        return "redirect:/caja";
    }
}
