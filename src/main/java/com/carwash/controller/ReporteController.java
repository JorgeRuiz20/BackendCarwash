package com.carwash.controller;

import com.carwash.dto.*;
import com.carwash.model.TipoReporte;
import com.carwash.service.ReporteService;
import com.carwash.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador para gestión de reportes y generación de PDFs
 */
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
public class ReporteController {
    
    private final ReporteService reporteService;
    private final PdfService pdfService;

    /**
     * Genera un reporte diario
     * POST /api/reportes/diario?fecha=2024-01-15
     */
    @PostMapping("/diario")
    public ResponseEntity<ApiResponse<ReporteDTO>> generarReporteDiario(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        ReporteDTO reporte = reporteService.generarReporteDiario(fecha);
        return ResponseEntity.ok(ApiResponse.success("Reporte diario generado exitosamente", reporte));
    }

    /**
     * Genera un reporte mensual
     * POST /api/reportes/mensual?mes=1&anio=2024
     */
    @PostMapping("/mensual")
    public ResponseEntity<ApiResponse<ReporteDTO>> generarReporteMensual(
            @RequestParam int mes,
            @RequestParam int anio) {
        ReporteDTO reporte = reporteService.generarReporteMensual(mes, anio);
        return ResponseEntity.ok(ApiResponse.success("Reporte mensual generado exitosamente", reporte));
    }

    /**
     * Lista los últimos reportes por tipo
     * GET /api/reportes?tipo=DIARIO
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReporteDTO>>> listarReportes(
            @RequestParam TipoReporte tipo) {
        List<ReporteDTO> reportes = reporteService.listarReportes(tipo);
        return ResponseEntity.ok(ApiResponse.success(reportes));
    }

    // ============================================
    // ENDPOINTS PARA GENERACIÓN DE PDFs
    // ============================================

    /**
     * Descarga un reporte diario en PDF
     * GET /api/reportes/pdf/diario?fecha=2024-01-15
     * 
     * @return PDF del reporte diario
     */
    @GetMapping("/pdf/diario")
    public ResponseEntity<byte[]> descargarReporteDiarioPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            // Generar el reporte
            ReporteDTO reporte = reporteService.generarReporteDiario(fecha);
            
            // Generar el PDF
            byte[] pdfBytes = pdfService.generarReporteDiarioPdf(reporte);
            
            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "reporte_diario_" + fecha.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Descarga un reporte de pagos en PDF por rango de fechas
     * GET /api/reportes/pdf/pagos?fechaInicio=2024-01-01&fechaFin=2024-01-31
     * 
     * @return PDF del reporte de pagos
     */
    @GetMapping("/pdf/pagos")
    public ResponseEntity<byte[]> descargarReportePagosPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        try {
            // Generar el PDF
            byte[] pdfBytes = pdfService.generarReportePagosPdf(fechaInicio, fechaFin);
            
            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = String.format("reporte_pagos_%s_a_%s.pdf", 
                fechaInicio.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                fechaFin.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Descarga un reporte de alertas por bahías en PDF
     * GET /api/reportes/pdf/alertas
     * 
     * @return PDF del reporte de alertas
     */
    @GetMapping("/pdf/alertas")
    public ResponseEntity<byte[]> descargarReporteAlertasPdf() {
        try {
            // Generar el PDF
            byte[] pdfBytes = pdfService.generarReporteAlertasPdf();
            
            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "reporte_alertas_bahias_" + 
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Descarga un reporte mensual en PDF
     * GET /api/reportes/pdf/mensual?mes=1&anio=2024
     * 
     * @return PDF del reporte mensual
     */
    @GetMapping("/pdf/mensual")
    public ResponseEntity<byte[]> descargarReporteMensualPdf(
            @RequestParam int mes,
            @RequestParam int anio) {
        try {
            // Generar el reporte mensual
            ReporteDTO reporte = reporteService.generarReporteMensual(mes, anio);
            
            // Reutilizar la lógica del reporte diario (funcionará para período)
            byte[] pdfBytes = pdfService.generarReporteDiarioPdf(reporte);
            
            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = String.format("reporte_mensual_%02d_%d.pdf", mes, anio);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}