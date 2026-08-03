package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.DocumentoEmpleado;
import com.fabrica.pagos.model.Empleado;
import com.fabrica.pagos.repository.DocumentoEmpleadoRepository;
import com.fabrica.pagos.repository.EmpleadoRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/documentos")
public class DocumentoController {

    private final DocumentoEmpleadoRepository documentoRepository;
    private final EmpleadoRepository empleadoRepository;

    public DocumentoController(DocumentoEmpleadoRepository documentoRepository,
                               EmpleadoRepository empleadoRepository) {
        this.documentoRepository = documentoRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @PostMapping("/subir")
    @PreAuthorize("hasRole('ADMIN')")
    public String subir(@RequestParam Long empleadoId,
                        @RequestParam("archivo") MultipartFile archivo,
                        @RequestParam(required = false) String tipo,
                        @RequestParam(required = false) String descripcion,
                        RedirectAttributes ra) {
        if (archivo == null || archivo.isEmpty()) {
            ra.addFlashAttribute("mensajeError", "Selecciona un archivo para subir");
            return "redirect:/empleados/detalle/" + empleadoId;
        }
        try {
            Empleado empleado = empleadoRepository.findById(empleadoId).orElseThrow();
            DocumentoEmpleado doc = new DocumentoEmpleado();
            doc.setEmpleado(empleado);
            doc.setNombre(archivo.getOriginalFilename());
            doc.setTipo(tipo);
            doc.setDescripcion(descripcion);
            doc.setContentType(archivo.getContentType());
            doc.setArchivo(archivo.getBytes());
            documentoRepository.save(doc);
            ra.addFlashAttribute("mensajeExito", "Documento subido correctamente");
        } catch (IOException e) {
            ra.addFlashAttribute("mensajeError", "No se pudo subir el documento");
        }
        return "redirect:/empleados/detalle/" + empleadoId;
    }

    @GetMapping("/descargar/{id}")
    public ResponseEntity<byte[]> descargar(@PathVariable Long id) {
        DocumentoEmpleado doc = documentoRepository.findById(id).orElseThrow();
        if (doc.getArchivo() == null) {
            return ResponseEntity.notFound().build();
        }
        String contentType = doc.getContentType() != null
                ? doc.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String nombre = doc.getNombre().replaceAll("[^a-zA-Z0-9._-]", "_");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .body(doc.getArchivo());
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        DocumentoEmpleado doc = documentoRepository.findById(id).orElseThrow();
        Long empleadoId = doc.getEmpleado().getId();
        documentoRepository.deleteById(id);
        ra.addFlashAttribute("mensajeExito", "Documento eliminado");
        return "redirect:/empleados/detalle/" + empleadoId;
    }
}
