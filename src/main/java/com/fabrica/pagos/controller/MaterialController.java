package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.Material;
import com.fabrica.pagos.model.MovimientoInventario;
import com.fabrica.pagos.repository.MaterialRepository;
import com.fabrica.pagos.repository.MovimientoInventarioRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/inventario")
public class MaterialController {

    private final MaterialRepository materialRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public MaterialController(MaterialRepository materialRepository,
                              MovimientoInventarioRepository movimientoRepository) {
        this.materialRepository = materialRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String bajo, Model model) {
        List<Material> materiales = (bajo != null && "true".equals(bajo))
                ? materialRepository.findByBajoStock()
                : materialRepository.findAllByOrderByNombreAsc();
        model.addAttribute("materiales", materiales);
        model.addAttribute("totalMateriales", materialRepository.count());
        model.addAttribute("valorInventario", materialRepository.sumValorInventario());
        model.addAttribute("bajoStock", materialRepository.countBajoStock());
        model.addAttribute("filtroBajo", "true".equals(bajo));
        return "inventario/listar";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        Material material = new Material();
        material.setUnidad("UN");
        material.setStockActual(BigDecimal.ZERO);
        model.addAttribute("material", material);
        return "inventario/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@ModelAttribute Material material, RedirectAttributes ra) {
        if (material.getCodigo() == null || material.getCodigo().isBlank()) {
            ra.addFlashAttribute("mensajeError", "El código es obligatorio");
            return "redirect:/inventario/nuevo";
        }
        if (material.getNombre() == null || material.getNombre().isBlank()) {
            ra.addFlashAttribute("mensajeError", "El nombre es obligatorio");
            return "redirect:/inventario/nuevo";
        }
        if (material.getPrecioUnitario() == null || material.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0
                || material.getStockMinimo() == null || material.getStockMinimo().compareTo(BigDecimal.ZERO) < 0
                || material.getStockActual() == null || material.getStockActual().compareTo(BigDecimal.ZERO) < 0) {
            ra.addFlashAttribute("mensajeError", "Los valores no pueden ser negativos");
            return "redirect:/inventario/nuevo";
        }
        if (materialRepository.findByCodigoIgnoreCase(material.getCodigo().trim())
                .filter(existente -> !existente.getId().equals(material.getId()))
                .isPresent()) {
            ra.addFlashAttribute("mensajeError", "Ya existe un material con ese código");
            return "redirect:/inventario/nuevo";
        }
        materialRepository.save(material);
        ra.addFlashAttribute("mensajeExito", "Material guardado correctamente");
        return "redirect:/inventario";
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Material material = materialRepository.findById(id).orElseThrow();
        model.addAttribute("material", material);
        model.addAttribute("movimientos", movimientoRepository.findByMaterialIdOrderByFechaDesc(id));
        return "inventario/detalle";
    }

    @PostMapping("/movimiento")
    @PreAuthorize("hasRole('ADMIN')")
    public String movimiento(@RequestParam Long materialId,
                             @RequestParam String tipo,
                             @RequestParam BigDecimal cantidad,
                             @RequestParam(required = false) LocalDate fecha,
                             @RequestParam(required = false) String motivo,
                             RedirectAttributes ra) {
        Material material = materialRepository.findById(materialId).orElseThrow();
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("mensajeError", "La cantidad debe ser mayor que cero");
            return "redirect:/inventario/detalle/" + materialId;
        }
        LocalDate fechaMov = fecha != null ? fecha : LocalDate.now();
        if ("SALIDA".equals(tipo)) {
            if (cantidad.compareTo(material.getStockActual()) > 0) {
                ra.addFlashAttribute("mensajeError", "Stock insuficiente para la salida");
                return "redirect:/inventario/detalle/" + materialId;
            }
            material.setStockActual(material.getStockActual().subtract(cantidad));
        } else {
            material.setStockActual(material.getStockActual().add(cantidad));
        }
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setMaterial(material);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setFecha(fechaMov);
        movimiento.setMotivo(motivo);
        materialRepository.save(material);
        movimientoRepository.save(movimiento);
        ra.addFlashAttribute("mensajeExito", "Movimiento registrado. Stock actual: " + material.getStockActual());
        return "redirect:/inventario/detalle/" + materialId;
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        materialRepository.deleteById(id);
        ra.addFlashAttribute("mensajeExito", "Material eliminado");
        return "redirect:/inventario";
    }
}
