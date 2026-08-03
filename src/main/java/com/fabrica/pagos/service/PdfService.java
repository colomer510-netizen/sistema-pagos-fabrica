package com.fabrica.pagos.service;

import com.fabrica.pagos.model.AsistenciaResumen;
import com.fabrica.pagos.model.DeduccionAplicada;
import com.fabrica.pagos.model.Empleado;
import com.fabrica.pagos.model.Empresa;
import com.fabrica.pagos.model.Recibo;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EmpresaService empresaService;

    public PdfService(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    public byte[] generarReporteNominaPdf(List<Recibo> recibos) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            String moneda = agregarEncabezado(document);

            Font titulo = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font subtitulo = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 11, Font.NORMAL);

            Paragraph p = new Paragraph("REPORTE DE NÓMINA", titulo);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);

            if (!recibos.isEmpty()) {
                Paragraph periodo = new Paragraph("Periodo: "
                        + recibos.get(0).getNomina().getPeriodoInicio().format(FECHA) + " al "
                        + recibos.get(0).getNomina().getPeriodoFin().format(FECHA)
                        + "   Nómina: " + recibos.get(0).getNomina().getNumero(), subtitulo);
                periodo.setAlignment(Element.ALIGN_CENTER);
                document.add(periodo);
            }
            document.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(7);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{2.2f, 4.5f, 2.0f, 2.0f, 2.6f, 2.6f, 2.8f});
            String[] headers = {"Código", "Empleado", "H. Normales", "H. Extras", "Bruto", "INSS", "Neto"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, subtitulo));
                cell.setBackgroundColor(new Color(230, 230, 230));
                cell.setPadding(2);
                tabla.addCell(cell);
            }

            BigDecimal totalNeto = BigDecimal.ZERO;
            for (Recibo r : recibos) {
                tabla.addCell(new Paragraph(r.getEmpleado().getCodigo(), normal));
                tabla.addCell(new Paragraph(r.getEmpleado().getNombreCompleto(), normal));
                tabla.addCell(new Paragraph(String.valueOf(r.getHorasNormales()), normal));
                tabla.addCell(new Paragraph(String.valueOf(r.getHorasExtras()), normal));
                tabla.addCell(new Paragraph(moneda + " " + formato(r.getSalarioBruto()), normal));
                tabla.addCell(new Paragraph(moneda + " " + formato(r.getDescuentoInss()), normal));
                tabla.addCell(new Paragraph(moneda + " " + formato(r.getSalarioNeto()), normal));
                totalNeto = totalNeto.add(r.getSalarioNeto());
            }
            document.add(tabla);

            document.add(new Paragraph(" "));
            Paragraph total = new Paragraph("TOTAL A PAGAR: " + moneda + " " + formato(totalNeto), subtitulo);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            document.close();
            return out.toByteArray();
        }
    }

    public byte[] generarReciboPdf(Recibo recibo) throws Exception {
        return generarReciboPdf(recibo, List.of());
    }

    public byte[] generarReciboPdf(Recibo recibo, List<DeduccionAplicada> deducciones) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            String moneda = agregarEncabezado(document);

            Font titulo = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font subtitulo = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 11, Font.NORMAL);

            Paragraph p = new Paragraph("RECIBO DE PAGO", titulo);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);

            Paragraph n = new Paragraph("Nómina: " + recibo.getNomina().getNumero(), subtitulo);
            n.setAlignment(Element.ALIGN_CENTER);
            document.add(n);
            document.add(new Paragraph(" "));

            PdfPTable datos = new PdfPTable(2);
            datos.setWidthPercentage(100);
            datos.setWidths(new float[]{1f, 2.5f});

            Empleado e = recibo.getEmpleado();
            agregarDato(datos, "Empleado", e.getNombreCompleto(), normal);
            agregarDato(datos, "Código", e.getCodigo(), normal);
            agregarDato(datos, "Cargo", e.getCargo() == null ? "-" : e.getCargo(), normal);
            agregarDato(datos, "Departamento", e.getDepartamento(), normal);
            agregarDato(datos, "Periodo", recibo.getNomina().getPeriodoInicio().format(FECHA)
                    + " al " + recibo.getNomina().getPeriodoFin().format(FECHA), normal);
            document.add(datos);

            document.add(new Paragraph(" "));
            PdfPTable detalles = new PdfPTable(2);
            detalles.setWidthPercentage(100);
            detalles.setWidths(new float[]{1f, 1.5f});

            agregarDato(detalles, "Horas normales", String.valueOf(recibo.getHorasNormales()), normal);
            agregarDato(detalles, "Horas extras", String.valueOf(recibo.getHorasExtras()), normal);
            agregarDato(detalles, "Salario bruto", moneda + " " + formato(recibo.getSalarioBruto()), normal);
            agregarDato(detalles, "INSS laboral", "- " + moneda + " " + formato(recibo.getDescuentoInss()), normal);
            for (DeduccionAplicada d : deducciones) {
                agregarDato(detalles, d.getNombre(), "- " + moneda + " " + formato(d.getMonto()), normal);
            }
            agregarDato(detalles, "Total deducciones", "- " + moneda + " "
                    + formato(recibo.getTotalDeducciones() == null ? recibo.getDescuentoInss() : recibo.getTotalDeducciones()), normal);
            agregarDato(detalles, "Salario neto", moneda + " " + formato(recibo.getSalarioNeto()), subtitulo);
            document.add(detalles);

            document.add(new Paragraph(" "));
            Paragraph firma = new Paragraph("_______________________________\nFirma del responsable", normal);
            firma.setAlignment(Element.ALIGN_RIGHT);
            document.add(firma);

            document.close();
            return out.toByteArray();
        }
    }

    public byte[] generarReporteAsistenciaPdf(List<AsistenciaResumen> resumen,
                                              java.time.LocalDate inicio, java.time.LocalDate fin) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            agregarEncabezado(document);

            Font titulo = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font subtitulo = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 11, Font.NORMAL);

            Paragraph p = new Paragraph("REPORTE DE ASISTENCIA", titulo);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);

            Paragraph periodo = new Paragraph("Periodo: " + inicio.format(FECHA) + " al " + fin.format(FECHA), subtitulo);
            periodo.setAlignment(Element.ALIGN_CENTER);
            document.add(periodo);
            document.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(7);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{2.0f, 4.5f, 3.0f, 2.6f, 2.6f, 2.4f, 2.8f});
            String[] headers = {"Código", "Empleado", "Cargo", "Departamento", "Días", "Horas", "Promedio"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, subtitulo));
                cell.setBackgroundColor(new Color(230, 230, 230));
                cell.setPadding(2);
                tabla.addCell(cell);
            }

            for (AsistenciaResumen r : resumen) {
                tabla.addCell(new Paragraph(r.getCodigo(), normal));
                tabla.addCell(new Paragraph(r.getNombreCompleto(), normal));
                tabla.addCell(new Paragraph(r.getCargo() == null ? "-" : r.getCargo(), normal));
                tabla.addCell(new Paragraph(r.getDepartamento(), normal));
                tabla.addCell(new Paragraph(String.valueOf(r.getDiasTrabajados()), normal));
                tabla.addCell(new Paragraph(String.valueOf(r.getHorasTotales()), normal));
                tabla.addCell(new Paragraph(String.format("%.2f", r.getDiasTrabajados() == 0
                        ? 0 : r.getHorasTotales() * 1.0 / r.getDiasTrabajados()), normal));
            }
            document.add(tabla);

            document.close();
            return out.toByteArray();
        }
    }

    private String agregarEncabezado(Document document) throws Exception {
        Empresa empresa = empresaService.obtener();
        String moneda = empresa.getMoneda() == null || empresa.getMoneda().isBlank() ? "C$" : empresa.getMoneda();

        if (empresa.getLogo() != null && empresa.getLogo().length > 0) {
            try {
                Image logo = Image.getInstance(empresa.getLogo());
                logo.scaleToFit(50, 50);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception ignorada) {
                // Si el logo no se puede procesar, se omite.
            }
        }

        Paragraph nombre = new Paragraph(empresa.getNombre() == null ? "" : empresa.getNombre(),
                new Font(Font.HELVETICA, 14, Font.BOLD));
        nombre.setAlignment(Element.ALIGN_CENTER);
        document.add(nombre);

        StringBuilder datos = new StringBuilder();
        if (empresa.getRuc() != null && !empresa.getRuc().isBlank()) {
            datos.append("RUC: ").append(empresa.getRuc());
        }
        if (empresa.getDireccion() != null && !empresa.getDireccion().isBlank()) {
            if (datos.length() > 0) datos.append("   |   ");
            datos.append(empresa.getDireccion());
        }
        if (empresa.getTelefono() != null && !empresa.getTelefono().isBlank()) {
            if (datos.length() > 0) datos.append("   |   ");
            datos.append("Tel: ").append(empresa.getTelefono());
        }
        if (empresa.getEmail() != null && !empresa.getEmail().isBlank()) {
            if (datos.length() > 0) datos.append("   |   ");
            datos.append(empresa.getEmail());
        }
        if (datos.length() > 0) {
            Paragraph info = new Paragraph(datos.toString(), new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(90, 90, 90)));
            info.setAlignment(Element.ALIGN_CENTER);
            document.add(info);
        }
        document.add(new Paragraph(" "));
        return moneda;
    }

    private void agregarDato(PdfPTable tabla, String clave, String valor, Font fuente) {
        PdfPCell c1 = new PdfPCell(new Paragraph(clave, fuente));
        c1.setBorderColor(Color.LIGHT_GRAY);
        c1.setPadding(4);
        PdfPCell c2 = new PdfPCell(new Paragraph(valor, fuente));
        c2.setBorderColor(Color.LIGHT_GRAY);
        c2.setPadding(4);
        tabla.addCell(c1);
        tabla.addCell(c2);
    }

    private String formato(BigDecimal valor) {
        return String.format("%,.2f", valor);
    }
}