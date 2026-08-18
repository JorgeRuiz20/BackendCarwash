package com.carwash.service;

import com.carwash.dto.ReporteDTO;
import com.carwash.model.view.VistaReporteAlertasBahias;
import com.carwash.model.view.VistaReportePagos;
import com.carwash.repository.view.VistaReporteAlertasBahiasRepository;
import com.carwash.repository.view.VistaReportePagosRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class PdfService {
    
    private final VistaReportePagosRepository vistaPagosRepository;
    private final VistaReporteAlertasBahiasRepository vistaAlertasRepository;
    
    // Colores corporativos
    private static final BaseColor COLOR_HEADER = new BaseColor(41, 128, 185);
    private static final BaseColor COLOR_ACCENT = new BaseColor(52, 152, 219);
    private static final BaseColor COLOR_SUCCESS = new BaseColor(39, 174, 96);
    private static final BaseColor COLOR_WARNING = new BaseColor(243, 156, 18);
    private static final BaseColor COLOR_DANGER = new BaseColor(231, 76, 60);
    
    // Fuentes
    private Font fontTitle;
    private Font fontSubtitle;
    private Font fontNormal;
    private Font fontBold;
    private Font fontSmall;
    
    // Constructor
    public PdfService(VistaReportePagosRepository vistaPagosRepository, 
                     VistaReporteAlertasBahiasRepository vistaAlertasRepository) {
        this.vistaPagosRepository = vistaPagosRepository;
        this.vistaAlertasRepository = vistaAlertasRepository;
    }
    
    @PostConstruct
    private void initializeFonts() {
        try {
            fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COLOR_HEADER);
            fontSubtitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, COLOR_ACCENT);
            fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
            fontSmall = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);
        } catch (Exception e) {
            log.error("Error inicializando fuentes", e);
        }
    }
    
    /**
     * Genera un reporte de pagos en PDF
     */
    public byte[] generarReportePagosPdf(LocalDate fechaInicio, LocalDate fechaFin) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        
        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            
            // Encabezado
            agregarEncabezado(document, "REPORTE DE PAGOS", fechaInicio, fechaFin);
            
            // Obtener datos
            LocalDateTime inicio = fechaInicio.atStartOfDay();
            LocalDateTime fin = fechaFin.atTime(23, 59, 59);
            List<VistaReportePagos> pagos = vistaPagosRepository.findByFechaPagoBetween(inicio, fin);
            
            // Resumen ejecutivo
            agregarResumenPagos(document, pagos);
            
            // Tabla detallada
            agregarTablaPagos(document, pagos);
            
            // Pie de página
            agregarPiePagina(document);
            
        } finally {
            document.close();
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Genera un reporte de alertas por bahías en PDF
     */
    public byte[] generarReporteAlertasPdf() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        
        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            
            // Encabezado
            LocalDate hoy = LocalDate.now();
            agregarEncabezado(document, "REPORTE DE ALERTAS POR BAHÍAS", hoy, hoy);
            
            // Obtener datos
            List<VistaReporteAlertasBahias> alertas = vistaAlertasRepository.findAllOrderByTotalAlertas();
            
            // Resumen de alertas críticas
            agregarResumenAlertas(document, alertas);
            
            // Tabla detallada
            agregarTablaAlertas(document, alertas);
            
            // Pie de página
            agregarPiePagina(document);
            
        } finally {
            document.close();
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Genera un reporte diario completo en PDF
     */
    public byte[] generarReporteDiarioPdf(ReporteDTO reporte) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        
        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            
            // Encabezado
            agregarEncabezado(document, "REPORTE DIARIO - CARWASH SOLUTIONS", 
                            reporte.getFechaInicio(), reporte.getFechaFin());
            
            // Métricas principales
            agregarMetricasReporte(document, reporte);
            
            // Obtener datos detallados del día
            LocalDateTime inicio = reporte.getFechaInicio().atStartOfDay();
            LocalDateTime fin = reporte.getFechaFin().atTime(23, 59, 59);
            
            // ✅ CORREGIDO: Usar findPagosCompletadosByFecha
            List<VistaReportePagos> pagos = vistaPagosRepository.findPagosCompletadosByFecha(inicio, fin);
            
            if (!pagos.isEmpty()) {
                document.add(new Paragraph(" ")); // Espacio
                Paragraph subtitle = new Paragraph("DETALLE DE OPERACIONES", fontSubtitle);
                subtitle.setSpacingBefore(10);
                document.add(subtitle);
                
                agregarTablaPagos(document, pagos);
            }
            
            // Alertas del día
            List<VistaReporteAlertasBahias> alertas = vistaAlertasRepository.findBahiasConAlertasCriticas();
            if (!alertas.isEmpty()) {
                document.add(new Paragraph(" ")); // Espacio
                Paragraph alertTitle = new Paragraph("ALERTAS REGISTRADAS", fontSubtitle);
                alertTitle.setSpacingBefore(10);
                document.add(alertTitle);
                
                agregarTablaAlertas(document, alertas);
            }
            
            // Pie de página
            agregarPiePagina(document);
            
        } finally {
            document.close();
        }
        
        return baos.toByteArray();
    }
    
    private void agregarEncabezado(Document document, String titulo, LocalDate fechaInicio, LocalDate fechaFin) throws DocumentException {
        // Logo o título principal
        Paragraph title = new Paragraph(titulo, fontTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(5);
        document.add(title);
        
        // Subtítulo con fechas
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String periodo = fechaInicio.equals(fechaFin) 
            ? "Fecha: " + fechaInicio.format(formatter)
            : "Período: " + fechaInicio.format(formatter) + " al " + fechaFin.format(formatter);
        
        Paragraph subtitle = new Paragraph(periodo, fontNormal);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(5);
        document.add(subtitle);
        
        // Fecha de generación
        Paragraph generado = new Paragraph(
            "Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), 
            fontSmall
        );
        generado.setAlignment(Element.ALIGN_CENTER);
        generado.setSpacingAfter(15);
        document.add(generado);
        
        // Línea separadora
        LineSeparator line = new LineSeparator();
        line.setLineColor(COLOR_HEADER);
        document.add(new Chunk(line));
        document.add(new Paragraph(" ")); // Espacio
    }
    
    private void agregarResumenPagos(Document document, List<VistaReportePagos> pagos) throws DocumentException {
        Paragraph titulo = new Paragraph("RESUMEN EJECUTIVO", fontSubtitle);
        titulo.setSpacingBefore(10);
        document.add(titulo);
        
        // Calcular métricas
        long totalPagos = pagos.size();
        
        // ✅ CORREGIDO: "COMPLETADO" en lugar de "EXITOSO"
        BigDecimal totalIngresos = pagos.stream()
            .filter(p -> "COMPLETADO".equals(p.getEstadoPago()))
            .map(VistaReportePagos::getMonto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long pagosTarjeta = pagos.stream().filter(p -> "TARJETA".equals(p.getMetodoPago())).count();
        long pagosYape = pagos.stream().filter(p -> "YAPE".equals(p.getMetodoPago())).count();
        long pagosEfectivo = pagos.stream().filter(p -> "EFECTIVO".equals(p.getMetodoPago())).count();
        
        // Tabla de resumen
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        
        agregarCeldaResumen(table, "Total de Pagos:", String.valueOf(totalPagos));
        agregarCeldaResumen(table, "Ingresos Totales:", "S/ " + totalIngresos.setScale(2, RoundingMode.HALF_UP).toString());
        agregarCeldaResumen(table, "Pagos con Tarjeta:", String.valueOf(pagosTarjeta));
        agregarCeldaResumen(table, "Pagos con Yape:", String.valueOf(pagosYape));
        agregarCeldaResumen(table, "Pagos en Efectivo:", String.valueOf(pagosEfectivo));
        
        if (totalPagos > 0) {
            BigDecimal ticketPromedio = totalIngresos.divide(BigDecimal.valueOf(totalPagos), 2, RoundingMode.HALF_UP);
            agregarCeldaResumen(table, "Ticket Promedio:", "S/ " + ticketPromedio.toString());
        }
        
        document.add(table);
    }
    
    private void agregarTablaPagos(Document document, List<VistaReportePagos> pagos) throws DocumentException {
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 2.5f, 2f, 1.5f, 1.5f, 1.5f, 2f});
        table.setSpacingBefore(10);
        
        // Encabezados
        agregarCeldaHeader(table, "Fecha");
        agregarCeldaHeader(table, "Cliente");
        agregarCeldaHeader(table, "Servicio");
        agregarCeldaHeader(table, "Bahía");
        agregarCeldaHeader(table, "Método");
        agregarCeldaHeader(table, "Estado");
        agregarCeldaHeader(table, "Monto");
        
        // Datos
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        for (VistaReportePagos pago : pagos) {
            table.addCell(new PdfPCell(new Phrase(pago.getFechaPago().format(formatter), fontSmall)));
            table.addCell(new PdfPCell(new Phrase(pago.getClienteNombre(), fontSmall)));
            table.addCell(new PdfPCell(new Phrase(pago.getServicioNombre(), fontSmall)));
            table.addCell(new PdfPCell(new Phrase(pago.getBahiaNumero() != null ? pago.getBahiaNumero() : "-", fontSmall)));
            table.addCell(new PdfPCell(new Phrase(pago.getMetodoPago(), fontSmall)));
            
            // ✅ CORREGIDO: "COMPLETADO" en lugar de "EXITOSO"
            PdfPCell estadoCell = new PdfPCell(new Phrase(pago.getEstadoPago(), fontSmall));
            if ("COMPLETADO".equals(pago.getEstadoPago())) {
                estadoCell.setBackgroundColor(new BaseColor(220, 255, 220));
            } else if ("FALLIDO".equals(pago.getEstadoPago())) {
                estadoCell.setBackgroundColor(new BaseColor(255, 220, 220));
            }
            table.addCell(estadoCell);
            
            table.addCell(new PdfPCell(new Phrase("S/ " + pago.getMonto().setScale(2, RoundingMode.HALF_UP).toString(), fontSmall)));
        }
        
        document.add(table);
    }
    
    private void agregarCeldaResumen(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, fontBold));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, fontNormal));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }
    
    private void agregarCeldaHeader(PdfPTable table, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fontBold));
        cell.setBackgroundColor(COLOR_HEADER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }
    
    private void agregarPiePagina(Document document) throws DocumentException {
        document.add(new Paragraph(" ")); // Espacio
        LineSeparator line = new LineSeparator();
        line.setLineColor(COLOR_HEADER);
        document.add(new Chunk(line));
        
        Paragraph footer = new Paragraph(
            "CarWash Solutions © 2025 - Sistema de Gestión Automatizado", 
            fontSmall
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(5);
        document.add(footer);
    }
    
    private void agregarMetricasReporte(Document document, ReporteDTO reporte) throws DocumentException {
        Paragraph titulo = new Paragraph("MÉTRICAS DEL PERÍODO", fontSubtitle);
        titulo.setSpacingBefore(10);
        document.add(titulo);
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        
        agregarCeldaResumen(table, "Total Reservas:", String.valueOf(reporte.getTotalReservas()));
        agregarCeldaResumen(table, "Total Ingresos:", "S/ " + reporte.getIngresoTotal().setScale(2, RoundingMode.HALF_UP));
        agregarCeldaResumen(table, "Servicios Realizados:", String.valueOf(reporte.getTotalReservas()));
        
        document.add(table);
    }
    
    private void agregarResumenAlertas(Document document, List<VistaReporteAlertasBahias> alertas) throws DocumentException {
        Paragraph titulo = new Paragraph("RESUMEN DE ALERTAS", fontSubtitle);
        titulo.setSpacingBefore(10);
        document.add(titulo);
        
        long totalCriticas = alertas.stream()
            .mapToLong(VistaReporteAlertasBahias::getTotalAlertasCriticas)
            .sum();
        
        long totalAdvertencias = alertas.stream()
            .mapToLong(VistaReporteAlertasBahias::getTotalAlertasAdvertencia)
            .sum();
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        
        agregarCeldaResumen(table, "Alertas Críticas:", String.valueOf(totalCriticas));
        agregarCeldaResumen(table, "Alertas Advertencia:", String.valueOf(totalAdvertencias));
        agregarCeldaResumen(table, "Bahías Afectadas:", String.valueOf(alertas.size()));
        
        document.add(table);
    }
    
    private void agregarTablaAlertas(Document document, List<VistaReporteAlertasBahias> alertas) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 2f, 1.5f, 1.5f, 2f});
        table.setSpacingBefore(10);
        
        // Encabezados
        agregarCeldaHeader(table, "Bahía");
        agregarCeldaHeader(table, "Nombre");
        agregarCeldaHeader(table, "Críticas");
        agregarCeldaHeader(table, "Advertencias");
        agregarCeldaHeader(table, "Sensores Afectados");
        
        // Datos
        for (VistaReporteAlertasBahias alerta : alertas) {
            table.addCell(new PdfPCell(new Phrase(alerta.getBahiaNumero(), fontSmall)));
            table.addCell(new PdfPCell(new Phrase(alerta.getBahiaNombre(), fontSmall)));
            
            PdfPCell criticasCell = new PdfPCell(new Phrase(String.valueOf(alerta.getTotalAlertasCriticas()), fontSmall));
            if (alerta.getTotalAlertasCriticas() > 0) {
                criticasCell.setBackgroundColor(new BaseColor(255, 220, 220));
            }
            table.addCell(criticasCell);
            
            PdfPCell advertenciasCell = new PdfPCell(new Phrase(String.valueOf(alerta.getTotalAlertasAdvertencia()), fontSmall));
            if (alerta.getTotalAlertasAdvertencia() > 0) {
                advertenciasCell.setBackgroundColor(new BaseColor(255, 243, 205));
            }
            table.addCell(advertenciasCell);
            
            String sensores = alerta.getSensoresAfectados() != null ? alerta.getSensoresAfectados() : "-";
            table.addCell(new PdfPCell(new Phrase(sensores, fontSmall)));
        }
        
        document.add(table);
    }
}