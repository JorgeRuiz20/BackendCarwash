package com.carwash.controller;

import com.carwash.dto.*;
import com.carwash.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('OPERADOR', 'ADMINISTRADOR')")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardDTO>> obtenerDashboard() {
        DashboardDTO dashboard = dashboardService.obtenerDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
