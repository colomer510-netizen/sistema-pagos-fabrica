package com.fabrica.pagos.controller;

import com.fabrica.pagos.repository.EmpleadoRepository;
import com.fabrica.pagos.repository.NominaRepository;
import com.fabrica.pagos.repository.ReciboRepository;
import com.fabrica.pagos.service.ExcelService;
import com.fabrica.pagos.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final EmpleadoRepository empleadoRepository;
    private final NominaRepository nominaRepository;
    private final ReciboRepository reciboRepository;
    private final ExcelService excelService;
    private final PdfService pdfService;

    public ReporteController(EmpleadoRepository empleadoRepository,
                             NominaRepository nominaRepository,
                             ReciboRepository reciboRepository,
                             ExcelService excelService,
                             PdfService pdfService) {
        this.empleadoRepository = empleadoRepository;
        this.nominaRepository = nominaRepository;
        this.reciboRepository = reciboRepository;
        this.excelService = excelService;
        this.pdfService = pdfService;
    }

    @GetMapping
    public String reportes(Model model) {
        model.addAttribute("nominas", nominaRepository.findAllByOrderByFechaGeneracionDesc());
        model.addAttribute("hoy", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        return "reportes";
    }

    @GetMapping("/empleados.xlsx")
    public ResponseEntity<byte[]> exportarEmpleadosExcel() throws Exception {
        byte[] datos = excelService.exportarEmpleados(empleadoRepository.findAllByOrderByCodigoAsc());
        return respuestaArchivo(datos, "empleados.xlsx",
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @GetMapping("/nomina.xlsx")
    public ResponseEntity<byte[]> exportarNominaExcel(@RequestParam(required = false) Long nominaId) throws Exception {
        var recibos = nominaId == null
                ? reciboRepository.findAll()
                : reciboRepository.findByNominaIdOrderByEmpleadoCodigoAsc(nominaId);
        byte[] datos = excelService.exportarRecibos(recibos);
        return respuestaArchivo(datos, "nomina.xlsx",
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @GetMapping("/nomina.pdf")
    public ResponseEntity<byte[]> exportarNominaPdf(@RequestParam Long nominaId) throws Exception {
        var recibos = reciboRepository.findByNominaIdOrderByEmpleadoCodigoAsc(nominaId);
        byte[] datos = pdfService.generarReporteNominaPdf(recibos);
        return respuestaArchivo(datos, "reporte-nomina.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping("/recibo/{id}.pdf")
    public ResponseEntity<byte[]> exportarReciboPdf(@PathVariable Long id) throws Exception {
        var recibo = reciboRepository.findById(id).orElseThrow();
        byte[] datos = pdfService.generarReciboPdf(recibo);
        return respuestaArchivo(datos, "recibo.pdf", MediaType.APPLICATION_PDF);
    }

    private ResponseEntity<byte[]> respuestaArchivo(byte[] datos, String nombreArchivo, MediaType mediaType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(mediaType)
                .body(datos);
    }
}
