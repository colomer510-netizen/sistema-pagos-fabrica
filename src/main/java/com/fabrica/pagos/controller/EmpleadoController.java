package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.Empleado;
import com.fabrica.pagos.repository.DocumentoEmpleadoRepository;
import com.fabrica.pagos.repository.EmpleadoRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;

@Controller
@RequestMapping("/empleados")
public class EmpleadoController {

    private final EmpleadoRepository empleadoRepository;
    private final DocumentoEmpleadoRepository documentoEmpleadoRepository;

    public EmpleadoController(EmpleadoRepository empleadoRepository,
                              DocumentoEmpleadoRepository documentoEmpleadoRepository) {
        this.empleadoRepository = empleadoRepository;
        this.documentoEmpleadoRepository = documentoEmpleadoRepository;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String busqueda, Model model) {
        var empleados = empleadoRepository.findAllByOrderByCodigoAsc();
        if (busqueda != null && !busqueda.isBlank()) {
            String q = busqueda.trim().toLowerCase();
            empleados = empleados.stream()
                    .filter(e -> e.getNombreCompleto().toLowerCase().contains(q)
                            || e.getCodigo().toLowerCase().contains(q)
                            || (e.getCargo() != null && e.getCargo().toLowerCase().contains(q))
                            || e.getDepartamento().toLowerCase().contains(q))
                    .toList();
        }
        model.addAttribute("empleados", empleados);
        model.addAttribute("busqueda", busqueda);
        return "empleados/listar";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        Empleado empleado = new Empleado();
        empleado.setCodigo(generarCodigo());
        empleado.setFechaContratacion(LocalDate.now());
        model.addAttribute("empleado", empleado);
        model.addAttribute("modo", "nuevo");
        return "empleados/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@Valid @ModelAttribute Empleado empleado, BindingResult result,
                          @RequestParam(value = "foto", required = false) MultipartFile foto,
                          RedirectAttributes redirect, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("modo", empleado.getId() == null ? "nuevo" : "editar");
            return "empleados/form";
        }
        try {
            if (foto != null && !foto.isEmpty()) {
                empleado.setFoto(foto.getBytes());
            } else if (empleado.getId() != null) {
                empleadoRepository.findById(empleado.getId())
                        .ifPresent(anterior -> empleado.setFoto(anterior.getFoto()));
            }
            Empleado guardado = empleadoRepository.save(empleado);
            redirect.addFlashAttribute("mensajeExito",
                    "Empleado " + guardado.getCodigo() + " " + guardado.getNombreCompleto() + " guardado correctamente");
            return "redirect:/empleados";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("errorCodigo", "El código " + empleado.getCodigo() + " ya está en uso");
            model.addAttribute("modo", empleado.getId() == null ? "nuevo" : "editar");
            return "empleados/form";
        } catch (IOException e) {
            model.addAttribute("mensajeError", "No se pudo procesar la foto");
            model.addAttribute("modo", empleado.getId() == null ? "nuevo" : "editar");
            return "empleados/form";
        }
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editar(@PathVariable Long id, Model model) {
        Empleado empleado = empleadoRepository.findById(id).orElseThrow();
        model.addAttribute("empleado", empleado);
        model.addAttribute("modo", "editar");
        return "empleados/form";
    }

    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        empleadoRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("transExito", "Empleado eliminado");
        return "redirect:/empleados";
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("empleado", empleadoRepository.findById(id).orElseThrow());
        model.addAttribute("documentos", documentoEmpleadoRepository.findByEmpleadoIdOrderByFechaSubidaDesc(id));
        return "empleados/detalle";
    }

    @GetMapping("/foto/{id}")
    public ResponseEntity<byte[]> foto(@PathVariable Long id) {
        Empleado empleado = empleadoRepository.findById(id).orElseThrow();
        if (empleado.getFoto() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                .body(empleado.getFoto());
    }

    private String generarCodigo() {
        long count = empleadoRepository.count();
        return "EMP-" + String.format("%03d", count + 1);
    }
}