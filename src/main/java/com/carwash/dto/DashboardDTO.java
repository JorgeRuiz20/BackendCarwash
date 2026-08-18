package com.carwash.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {
    private Integer reservasHoy;
    private Integer reservasPendientes;
    private Integer reservasEnProgreso;
    private BigDecimal ingresosHoy;
    private BigDecimal ingresosMes;
    private List<BahiaDTO> bahiasDisponibles;
    private List<ReservaDTO> proximasReservas;
    private List<SensorTelemetriaDTO> alertasActivas;
}
