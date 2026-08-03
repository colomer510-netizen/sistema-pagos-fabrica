package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.Empresa;
import com.fabrica.pagos.service.EmpresaService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/empresa")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String form(Model model) {
        model.addAttribute("empresa", empresaService.obtener());
        return "empresa/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@ModelAttribute Empresa empresa,
                          @RequestParam(value = "logoArchivo", required = false) MultipartFile logo,
                          RedirectAttributes ra) {
        if (empresa.getNombre() == null || empresa.getNombre().isBlank()) {
            ra.addFlashAttribute("mensajeError", "El nombre de la empresa es obligatorio");
            return "redirect:/empresa";
        }
        if (empresa.getMoneda() == null || empresa.getMoneda().isBlank()) {
            ra.addFlashAttribute("mensajeError", "El símbolo de moneda es obligatorio");
            return "redirect:/empresa";
        }
        Empresa actual = empresaService.obtener();
        if (logo != null && !logo.isEmpty()) {
            try {
                if (logo.getSize() > 2_000_000) {
                    ra.addFlashAttribute("mensajeError", "El logo no puede superar 2 MB");
                    return "redirect:/empresa";
                }
                actual.setLogo(logo.getBytes());
                actual.setLogoContentType(logo.getContentType());
            } catch (IOException e) {
                ra.addFlashAttribute("mensajeError", "No se pudo leer el archivo del logo");
                return "redirect:/empresa";
            }
        }
        actual.setNombre(empresa.getNombre());
        actual.setRuc(empresa.getRuc());
        actual.setDireccion(empresa.getDireccion());
        actual.setTelefono(empresa.getTelefono());
        actual.setEmail(empresa.getEmail());
        actual.setMoneda(empresa.getMoneda());
        empresaService.guardar(actual);
        ra.addFlashAttribute("mensajeExito", "Datos de la empresa actualizados correctamente");
        return "redirect:/empresa";
    }

    @PostMapping("/quitar-logo")
    @PreAuthorize("hasRole('ADMIN')")
    public String quitarLogo(RedirectAttributes ra) {
        Empresa empresa = empresaService.obtener();
        empresa.setLogo(null);
        empresa.setLogoContentType(null);
        empresaService.guardar(empresa);
        ra.addFlashAttribute("mensajeExito", "Logo eliminado");
        return "redirect:/empresa";
    }

    @GetMapping("/logo")
    public ResponseEntity<byte[]> logo() {
        Empresa empresa = empresaService.obtener();
        if (empresa.getLogo() == null || empresa.getLogo().length == 0) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = empresa.getLogoContentType() != null
                ? MediaType.parseMediaType(empresa.getLogoContentType())
                : MediaType.IMAGE_PNG;
        return ResponseEntity.ok().contentType(mediaType).body(empresa.getLogo());
    }
}
