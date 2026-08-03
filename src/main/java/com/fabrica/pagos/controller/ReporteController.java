package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.Asistencia;
import com.fabrica.pagos.model.AsistenciaResumen;
import com.fabrica.pagos.model.Empleado;
import com.fabrica.pagos.repository.AsistenciaRepository;
import com.fabrica.pagos.repository.DeduccionAplicadaRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final EmpleadoRepository empleadoRepository;
    private final NominaRepository nominaRepository;
    private final ReciboRepository reciboRepository;
    private final DeduccionAplicadaRepository deduccionAplicadaRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final ExcelService excelService;
    private final PdfService pdfService;

    public ReporteController(EmpleadoRepository empleadoRepository,
                             NominaRepository nominaRepository,
                             ReciboRepository reciboRepository,
                             DeduccionAplicadaRepository deduccionAplicadaRepository,
                             AsistenciaRepository asistenciaRepository,
                             ExcelService excelService,
                             PdfService pdfService) {
        this.empleadoRepository = empleadoRepository;
        this.nominaRepository = nominaRepository;
        this.reciboRepository = reciboRepository;
        this.deduccionAplicadaRepository = deduccionAplicadaRepository;
        this.asistenciaRepository = asistenciaRepository;
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
        var deducciones = deduccionAplicadaRepository.findByReciboIdOrderByIdAsc(id);
        byte[] datos = pdfService.generarReciboPdf(recibo, deducciones);
        return respuestaArchivo(datos, "recibo.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping("/asistencia")
    public String reporteAsistencia(@RequestParam(required = false) LocalDate inicio,
                                    @RequestParam(required = false) LocalDate fin,
                                    Model model) {
        LocalDate i = inicio == null ? LocalDate.now().withDayOfMonth(1) : inicio;
        LocalDate f = fin == null ? LocalDate.now() : fin;
        List<AsistenciaResumen> resumen = resumenAsistencia(i, f);
        List<Asistencia> detalle = asistenciaRepository.findByFechaBetweenOrderByFechaAsc(i, f);
        model.addAttribute("inicio", i);
        model.addAttribute("fin", f);
        model.addAttribute("resumen", resumen);
        model.addAttribute("detalle", detalle);
        model.addAttribute("hoy", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        return "reportes/asistencia";
    }

    @GetMapping("/asistencia.xlsx")
    public ResponseEntity<byte[]> exportarAsistenciaExcel(@RequestParam LocalDate inicio,
                                                          @RequestParam LocalDate fin) throws Exception {
        byte[] datos = excelService.exportarAsistencia(resumenAsistencia(inicio, fin), inicio, fin);
        return respuestaArchivo(datos, "asistencia.xlsx",
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @GetMapping("/asistencia.pdf")
    public ResponseEntity<byte[]> exportarAsistenciaPdf(@RequestParam LocalDate inicio,
                                                        @RequestParam LocalDate fin) throws Exception {
        byte[] datos = pdfService.generarReporteAsistenciaPdf(resumenAsistencia(inicio, fin), inicio, fin);
        return respuestaArchivo(datos, "asistencia.pdf", MediaType.APPLICATION_PDF);
    }

    private List<AsistenciaResumen> resumenAsistencia(LocalDate inicio, LocalDate fin) {
        List<Asistencia> registros = asistenciaRepository.findByFechaBetweenOrderByFechaAsc(inicio, fin);
        Map<Long, AsistenciaResumen> mapa = new LinkedHashMap<>();
        for (Asistencia a : registros) {
            Empleado e = a.getEmpleado();
            AsistenciaResumen r = mapa.computeIfAbsent(e.getId(), k -> new AsistenciaResumen(
                    e.getCodigo(), e.getNombreCompleto(), e.getCargo(), e.getDepartamento(), 0, 0));
            r.setHorasTotales(r.getHorasTotales() + a.getHorasTrabajadas());
            r.setDiasTrabajados(r.getDiasTrabajados() + 1);
        }
        return new ArrayList<>(mapa.values());
    }

    private ResponseEntity<byte[]> respuestaArchivo(byte[] datos, String nombreArchivo, MediaType mediaType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(mediaType)
                .body(datos);
    }
}
