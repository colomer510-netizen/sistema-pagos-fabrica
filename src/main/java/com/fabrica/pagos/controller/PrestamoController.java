package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.Prestamo;
import com.fabrica.pagos.repository.EmpleadoRepository;
import com.fabrica.pagos.repository.PrestamoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/prestamos")
public class PrestamoController {

    private final PrestamoRepository prestamoRepository;
    private final EmpleadoRepository empleadoRepository;

    public PrestamoController(PrestamoRepository prestamoRepository, EmpleadoRepository empleadoRepository) {
        this.prestamoRepository = prestamoRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("prestamos", prestamoRepository.findAllByOrderByFechaDesc());
        return "prestamos/listar";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        model.addAttribute("prestamo", new Prestamo());
        model.addAttribute("modo", "nuevo");
        model.addAttribute("empleados", empleadoRepository.findByActivoTrueOrderByCodigoAsc());
        return "prestamos/form";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("prestamo", prestamoRepository.findById(id).orElseThrow());
        model.addAttribute("modo", "editar");
        model.addAttribute("empleados", empleadoRepository.findAllByOrderByCodigoAsc());
        return "prestamos/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@ModelAttribute Prestamo prestamo, RedirectAttributes ra) {
        if (prestamo.getMonto() == null || prestamo.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("mensajeError", "El monto debe ser mayor que cero");
            return "redirect:/prestamos/nuevo";
        }
        if (prestamo.getId() == null) {
            prestamo.setSaldo(prestamo.getMonto());
        }
        prestamoRepository.save(prestamo);
        ra.addFlashAttribute("mensajeExito", "Préstamo guardado correctamente");
        return "redirect:/prestamos";
    }

    @PostMapping("/abonar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String abonar(@PathVariable Long id, @RequestParam BigDecimal monto, RedirectAttributes ra) {
        Prestamo p = prestamoRepository.findById(id).orElseThrow();
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("mensajeError", "El abono debe ser mayor que cero");
            return "redirect:/prestamos";
        }
        BigDecimal nuevoSaldo = p.getSaldo().subtract(monto);
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) <= 0) {
            p.setSaldo(BigDecimal.ZERO);
            p.setActivo(false);
        } else {
            p.setSaldo(nuevoSaldo);
        }
        prestamoRepository.save(p);
        ra.addFlashAttribute("mensajeExito", "Abono registrado. Saldo actual: C$ " + p.getSaldo());
        return "redirect:/prestamos";
    }

    @PostMapping("/estado/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes ra) {
        Prestamo p = prestamoRepository.findById(id).orElseThrow();
        p.setActivo(!Boolean.TRUE.equals(p.getActivo()));
        prestamoRepository.save(p);
        ra.addFlashAttribute("mensajeExito", "Estado actualizado");
        return "redirect:/prestamos";
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        prestamoRepository.deleteById(id);
        ra.addFlashAttribute("mensajeExito", "Préstamo eliminado");
        return "redirect:/prestamos";
    }
}
