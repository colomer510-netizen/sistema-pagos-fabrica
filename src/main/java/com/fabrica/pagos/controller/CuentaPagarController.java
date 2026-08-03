package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.CuentaPagar;
import com.fabrica.pagos.repository.CuentaPagarRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/cuentas-pagar")
public class CuentaPagarController {

    private final CuentaPagarRepository cuentaRepository;

    public CuentaPagarController(CuentaPagarRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String estado, Model model) {
        var cuentas = cuentaRepository.findAllByOrderByFechaVencimientoDesc();
        if (estado != null && !estado.isBlank()) {
            cuentas = cuentas.stream().filter(c -> estado.equals(c.getEstado())).toList();
        }
        model.addAttribute("cuentas", cuentas);
        model.addAttribute("totalPendiente", cuentaRepository.totalPendiente());
        model.addAttribute("estadoFiltro", estado);
        return "cuentaspagar/listar";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        CuentaPagar cuenta = new CuentaPagar();
        cuenta.setFechaFactura(LocalDate.now());
        cuenta.setFechaVencimiento(LocalDate.now().plusDays(30));
        model.addAttribute("cuenta", cuenta);
        return "cuentaspagar/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@ModelAttribute CuentaPagar cuenta, RedirectAttributes ra) {
        if (cuenta.getMonto() == null || cuenta.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("mensajeError", "El monto debe ser mayor que cero");
            return "redirect:/cuentas-pagar/nuevo";
        }
        if (cuenta.getFechaVencimiento() == null) {
            ra.addFlashAttribute("mensajeError", "La fecha de vencimiento es obligatoria");
            return "redirect:/cuentas-pagar/nuevo";
        }
        cuentaRepository.save(cuenta);
        ra.addFlashAttribute("mensajeExito", "Cuenta por pagar guardada correctamente");
        return "redirect:/cuentas-pagar";
    }

    @PostMapping("/marcar-pagada/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String marcarPagada(@PathVariable Long id, RedirectAttributes ra) {
        CuentaPagar cuenta = cuentaRepository.findById(id).orElseThrow();
        cuenta.setEstado("PAGADA");
        cuenta.setFechaPago(LocalDate.now());
        cuentaRepository.save(cuenta);
        ra.addFlashAttribute("mensajeExito", "Cuenta marcada como pagada");
        return "redirect:/cuentas-pagar";
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        cuentaRepository.deleteById(id);
        ra.addFlashAttribute("mensajeExito", "Cuenta por pagar eliminada");
        return "redirect:/cuentas-pagar";
    }
}
