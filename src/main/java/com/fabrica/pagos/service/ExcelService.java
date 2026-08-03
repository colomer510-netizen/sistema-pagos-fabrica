package com.fabrica.pagos.service;

import com.fabrica.pagos.model.AsistenciaResumen;
import com.fabrica.pagos.model.Empleado;
import com.fabrica.pagos.model.Recibo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] exportarEmpleados(List<Empleado> empleados) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Empleados");
            CellStyle encabezado = estiloEncabezado(wb);
            String[] columnas = {"Código", "Nombre", "Apellido", "Cédula", "Cargo", "Departamento",
                    "Tarifa/Hora", "Fecha Contratación", "Estado"};
            crearFilaEncabezado(sheet, encabezado, columnas);

            int fila = 1;
            for (Empleado e : empleados) {
                Row row = sheet.createRow(fila++);
                row.createCell(0).setCellValue(e.getCodigo());
                row.createCell(1).setCellValue(e.getNombre());
                row.createCell(2).setCellValue(e.getApellido());
                row.createCell(3).setCellValue(e.getCedula() == null ? "" : e.getCedula());
                row.createCell(4).setCellValue(e.getCargo() == null ? "" : e.getCargo());
                row.createCell(5).setCellValue(e.getDepartamento());
                row.createCell(6).setCellValue(e.getTarifaHora().doubleValue());
                row.createCell(7).setCellValue(e.getFechaContratacion().format(FECHA));
                row.createCell(8).setCellValue(e.getActivo() ? "Activo" : "Inactivo");
            }
            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportarRecibos(List<Recibo> recibos) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Nómina");
            CellStyle encabezado = estiloEncabezado(wb);
            String[] columnas = {"Código", "Empleado", "Horas Normales", "Horas Extras",
                    "Salario Bruto", "INSS Laboral", "Salario Neto", "Periodo"};
            crearFilaEncabezado(sheet, encabezado, columnas);

            int fila = 1;
            for (Recibo r : recibos) {
                Row row = sheet.createRow(fila++);
                row.createCell(0).setCellValue(r.getEmpleado().getCodigo());
                row.createCell(1).setCellValue(r.getEmpleado().getNombreCompleto());
                row.createCell(2).setCellValue(r.getHorasNormales());
                row.createCell(3).setCellValue(r.getHorasExtras());
                row.createCell(4).setCellValue(r.getSalarioBruto().doubleValue());
                row.createCell(5).setCellValue(r.getDescuentoInss().doubleValue());
                row.createCell(6).setCellValue(r.getSalarioNeto().doubleValue());
                row.createCell(7).setCellValue(r.getNomina().getPeriodoInicio().format(FECHA)
                        + " al " + r.getNomina().getPeriodoFin().format(FECHA));
            }
            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportarAsistencia(List<AsistenciaResumen> resumen,
                                     LocalDate inicio, LocalDate fin) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Asistencia");
            CellStyle encabezado = estiloEncabezado(wb);
            String[] columnas = {"Código", "Empleado", "Cargo", "Departamento",
                    "Días trabajados", "Horas totales", "Promedio horas/día"};
            crearFilaEncabezado(sheet, encabezado, columnas);

            int fila = 1;
            for (AsistenciaResumen r : resumen) {
                Row row = sheet.createRow(fila++);
                row.createCell(0).setCellValue(r.getCodigo());
                row.createCell(1).setCellValue(r.getNombreCompleto());
                row.createCell(2).setCellValue(r.getCargo() == null ? "" : r.getCargo());
                row.createCell(3).setCellValue(r.getDepartamento());
                row.createCell(4).setCellValue(r.getDiasTrabajados());
                row.createCell(5).setCellValue(r.getHorasTotales());
                row.createCell(6).setCellValue(r.getDiasTrabajados() == 0 ? 0
                        : Math.round(r.getHorasTotales() * 100.0 / r.getDiasTrabajados()) / 100.0);
            }
            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    private CellStyle estiloEncabezado(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void crearFilaEncabezado(Sheet sheet, CellStyle estilo, String[] columnas) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < columnas.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(columnas[i]);
            cell.setCellStyle(estilo);
        }
    }
}
