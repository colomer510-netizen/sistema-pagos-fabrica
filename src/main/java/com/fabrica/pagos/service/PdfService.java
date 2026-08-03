package com.fabrica.pagos.service;

import com.fabrica.pagos.model.Empleado;
import com.fabrica.pagos.model.Recibo;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String moneda;

    public PdfService(@Value("${app.contabilidad.moneda:C$}") String moneda) {
        this.moneda = moneda;
    }

    public byte[] generarReporteNominaPdf(List<Recibo> recibos) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

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
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

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