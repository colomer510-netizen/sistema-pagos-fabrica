package com.fabrica.pagos.controller;

import com.fabrica.pagos.service.RespaldoService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.util.List;

@Controller
@RequestMapping("/respaldos")
public class RespaldoController {

    private final RespaldoService respaldoService;

    public RespaldoController(RespaldoService respaldoService) {
        this.respaldoService = respaldoService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String listar(Model model) {
        List<Path> respaldos = respaldoService.listarRespaldo();
        model.addAttribute("respaldos", respaldos);
        model.addAttribute("directorio", respaldoService.getDirectorioRespaldo().toString());
        return "respaldos/listar";
    }

    @PostMapping("/crear")
    @PreAuthorize("hasRole('ADMIN')")
    public String crear(RedirectAttributes ra) {
        try {
            String nombre = respaldoService.crearRespaldo();
            ra.addFlashAttribute("mensajeExito", "Respaldo creado: " + nombre);
        } catch (Exception e) {
            ra.addFlashAttribute("mensajeError", "No se pudo crear el respaldo: " + e.getMessage());
        }
        return "redirect:/respaldos";
    }

    @GetMapping("/descargar/{nombre}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> descargar(@PathVariable String nombre) {
        try {
            byte[] datos = respaldoService.descargar(nombre);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new ByteArrayResource(datos));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/eliminar/{nombre}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable String nombre, RedirectAttributes ra) {
        try {
            respaldoService.eliminar(nombre);
            ra.addFlashAttribute("mensajeExito", "Respaldo eliminado");
        } catch (Exception e) {
            ra.addFlashAttribute("mensajeError", "No se pudo eliminar el respaldo");
        }
        return "redirect:/respaldos";
    }
}
