package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.Nomina;
import com.fabrica.pagos.model.NominaPreviewItem;
import com.fabrica.pagos.repository.NominaRepository;
import com.fabrica.pagos.service.NominaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/nomina")
public class NominaController {

    private final NominaService nominaService;
    private final NominaRepository nominaRepository;

    public NominaController(NominaService nominaService, NominaRepository nominaRepository) {
        this.nominaService = nominaService;
        this.nominaRepository = nominaRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("nominas", nominaRepository.findAllByOrderByFechaGeneracionDesc());
        return "nomina/listar";
    }

    @GetMapping("/generar")
    @PreAuthorize("hasRole('ADMIN')")
    public String generar(Model model) {
        model.addAttribute("periodoInicio", LocalDate.now().withDayOfMonth(1));
        model.addAttribute("periodoFin", LocalDate.now());
        return "nomina/generar";
    }

    @GetMapping("/previa")
    @PreAuthorize("hasRole('ADMIN')")
    public String previa(@RequestParam LocalDate periodoInicio,
                         @RequestParam LocalDate periodoFin,
                         Model model,
                         RedirectAttributes ra) {
        try {
            var items = nominaService.previewNomina(periodoInicio, periodoFin);
            if (items.isEmpty()) {
                ra.addFlashAttribute("mensajeError",
                        "No hay asistencia registrada en este periodo para generar la nómina");
                return "redirect:/nomina/generar";
            }
            model.addAttribute("periodoInicio", periodoInicio);
            model.addAttribute("periodoFin", periodoFin);
            model.addAttribute("items", items);
            model.addAttribute("totalEmpleados", items.size());
            model.addAttribute("totalHoras", items.stream().mapToInt(NominaPreviewItem::getHorasNormales)
                    .sum() + items.stream().mapToInt(NominaPreviewItem::getHorasExtras).sum());
            model.addAttribute("totalPagar", items.stream()
                    .map(NominaPreviewItem::getSalarioNeto)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
            return "nomina/previa";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/nomina/generar";
        }
    }

    @PostMapping("/generar")
    @PreAuthorize("hasRole('ADMIN')")
    public String generarNomina(@RequestParam LocalDate periodoInicio,
                                @RequestParam LocalDate periodoFin,
                                RedirectAttributes redirectAttributes) {
        try {
            Nomina nomina = nominaService.generarNomina(periodoInicio, periodoFin);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Nómina " + nomina.getNumero() + " generada correctamente (" + nomina.getTotalEmpleados()
                            + " empleados, " + formatoCordobas(nomina.getTotalPagar()) + ")");
            return "redirect:/nomina/detalle/" + nomina.getId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/nomina/generar";
        }
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Nomina nomina = nominaRepository.findById(id).orElseThrow();
        model.addAttribute("nomina", nomina);
        model.addAttribute("recibos", nominaService.getRecibos(nomina));
        model.addAttribute("deduccionesPorRecibo", nominaService.getDeduccionesPorRecibo(nomina));
        return "nomina/detalle";
    }

    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        nominaService.eliminarNomina(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Nómina eliminada correctamente");
        return "redirect:/nomina";
    }

    private String formatoCordobas(java.math.BigDecimal valor) {
        return "C$ " + String.format("%,.2f", valor);
    }
}
