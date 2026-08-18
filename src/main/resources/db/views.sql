-- ============================================
-- VISTAS SQL GENERADAS DESDE LOS MODELOS JAVA
-- ============================================
-- Fecha: 2025-02-09
-- Sistema: CarWash Backend
-- IMPORTANTE: Estas vistas están generadas EXACTAMENTE según los modelos @Entity de las vistas
-- ============================================

-- ===== VISTA 1: vista_reporte_alertas_bahias =====
-- Modelo Java: VistaReporteAlertasBahias.java
-- Ubicación: src/main/java/com/carwash/model/view/VistaReporteAlertasBahias.java

CREATE OR REPLACE VIEW vista_reporte_alertas_bahias AS
SELECT 
    -- EXACTO según modelo: @Column(name = "bahia_id")
    b.id AS bahia_id,
    
    -- EXACTO según modelo: @Column(name = "bahia_numero")
    b.numero AS bahia_numero,
    
    -- EXACTO según modelo: @Column(name = "bahia_nombre")
    b.nombre AS bahia_nombre,
    
    -- EXACTO según modelo: @Column(name = "bahia_estado")
    b.estado AS bahia_estado,
    
    -- EXACTO según modelo: @Column(name = "total_alertas_criticas")
    COUNT(CASE WHEN st.nivel_alerta = 'CRITICA' THEN 1 END) AS total_alertas_criticas,
    
    -- EXACTO según modelo: @Column(name = "total_alertas_advertencia")
    COUNT(CASE WHEN st.nivel_alerta = 'ADVERTENCIA' THEN 1 END) AS total_alertas_advertencia,
    
    -- EXACTO según modelo: @Column(name = "total_alertas")
    COUNT(st.id) AS total_alertas,
    
    -- EXACTO según modelo: @Column(name = "ultima_alerta_fecha")
    MAX(st.fecha_lectura) AS ultima_alerta_fecha,
    
    -- EXACTO según modelo: @Column(name = "sensores_afectados")
    GROUP_CONCAT(DISTINCT st.tipo_sensor ORDER BY st.tipo_sensor) AS sensores_afectados,
    
    -- EXACTO según modelo: @Column(name = "promedio_nivel_agua")
    AVG(CASE WHEN st.tipo_sensor = 'NIVEL_AGUA' THEN st.valor END) AS promedio_nivel_agua,
    
    -- EXACTO según modelo: @Column(name = "promedio_presion_agua")
    AVG(CASE WHEN st.tipo_sensor = 'PRESION_AGUA' THEN st.valor END) AS promedio_presion_agua,
    
    -- EXACTO según modelo: @Column(name = "promedio_temperatura")
    AVG(CASE WHEN st.tipo_sensor = 'TEMPERATURA' THEN st.valor END) AS promedio_temperatura,
    
    -- EXACTO según modelo: @Column(name = "promedio_flujo_agua")
    AVG(CASE WHEN st.tipo_sensor = 'FLUJO_AGUA' THEN st.valor END) AS promedio_flujo_agua,
    
    -- EXACTO según modelo: @Column(name = "promedio_consumo_energia")
    AVG(CASE WHEN st.tipo_sensor = 'CONSUMO_ENERGIA' THEN st.valor END) AS promedio_consumo_energia
    
FROM bahias b
LEFT JOIN sensores_telemetria st 
    ON b.id = st.bahia_id 
    AND st.alerta_activa = TRUE
    AND st.fecha_lectura >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY b.id, b.numero, b.nombre, b.estado
ORDER BY total_alertas_criticas DESC, total_alertas_advertencia DESC;


-- ===== VISTA 2: vista_reporte_pagos =====
-- Modelo Java: VistaReportePagos.java
-- Ubicación: src/main/java/com/carwash/model/view/VistaReportePagos.java

CREATE OR REPLACE VIEW vista_reporte_pagos AS
SELECT 
    -- @Column(name = "pago_id") - @Id
    p.id AS pago_id,
    
    -- @Column(name = "fecha_pago")
    p.fecha_pago,
    
    -- @Column(name = "monto")
    p.monto,
    
    -- @Column(name = "metodo_pago")
    p.metodo_pago,
    
    -- @Column(name = "estado_pago")
    p.estado AS estado_pago,
    
    -- @Column(name = "numero_transaccion")
    -- IMPORTANTE: El modelo Pago tiene "transaccionId" que se convierte a "transaccion_id"
    -- pero la vista espera "numero_transaccion"
    p.transaccion_id AS numero_transaccion,
    
    -- @Column(name = "reserva_id")
    r.id AS reserva_id,
    
    -- @Column(name = "reserva_estado")
    r.estado AS reserva_estado,
    
    -- @Column(name = "fecha_reserva")
    r.fecha_reserva,
    
    -- @Column(name = "fecha_inicio")
    r.fecha_inicio,
    
    -- @Column(name = "fecha_fin")
    r.fecha_fin,
    
    -- @Column(name = "precio_servicio")
    r.precio_servicio,
    
    -- @Column(name = "descuento_aplicado")
    r.descuento_aplicado,
    
    -- @Column(name = "precio_total")
    r.precio_total,
    
    -- @Column(name = "vehiculo_placa")
    r.vehiculo_placa,
    
    -- @Column(name = "vehiculo_modelo")
    r.vehiculo_modelo,
    
    -- @Column(name = "cliente_nombre")
    CONCAT(u.nombre, ' ', u.apellido) AS cliente_nombre,
    
    -- @Column(name = "cliente_email")
    u.email AS cliente_email,
    
    -- @Column(name = "cliente_telefono")
    u.telefono AS cliente_telefono,
    
    -- @Column(name = "servicio_id")
    s.id AS servicio_id,
    
    -- @Column(name = "servicio_nombre")
    s.nombre AS servicio_nombre,
    
    -- @Column(name = "servicio_tipo")
    s.tipo AS servicio_tipo,
    
    -- @Column(name = "servicio_duracion")
    s.duracion_minutos AS servicio_duracion,
    
    -- @Column(name = "bahia_numero")
    b.numero AS bahia_numero,
    
    -- @Column(name = "bahia_nombre")
    b.nombre AS bahia_nombre,
    
    -- @Column(name = "cupon_codigo")
    c.codigo AS cupon_codigo,
    
    -- @Column(name = "cupon_tipo")
    c.tipo_descuento AS cupon_tipo,
    
    -- @Column(name = "cupon_valor")
    c.valor_descuento AS cupon_valor,
    
    -- @Column(name = "duracion_real_minutos")
    TIMESTAMPDIFF(MINUTE, r.fecha_inicio, r.fecha_fin) AS duracion_real_minutos

FROM pagos p
INNER JOIN reservas r ON p.reserva_id = r.id
INNER JOIN usuarios u ON r.cliente_id = u.id
INNER JOIN servicios s ON r.servicio_id = s.id
LEFT JOIN bahias b ON r.bahia_id = b.id
LEFT JOIN cupones c ON r.cupon_id = c.id
ORDER BY p.fecha_pago DESC;


-- ===== VISTA 3: vista_resumen_pagos_diario =====
-- Modelo Java: VistaResumenPagosDiario.java
-- Ubicación: src/main/java/com/carwash/model/view/VistaResumenPagosDiario.java

CREATE OR REPLACE VIEW vista_resumen_pagos_diario AS
SELECT 
    -- @Column(name = "fecha") - @Id
    DATE(p.fecha_pago) AS fecha,
    
    -- @Column(name = "total_pagos")
    COUNT(DISTINCT p.id) AS total_pagos,

    -- CRÍTICO: Corregido para coincidir con el enum EstadoPago.COMPLETADO
    -- @Column(name = "pagos_completados")
    COUNT(DISTINCT CASE WHEN p.estado = 'COMPLETADO' THEN p.id END) AS pagos_completados,
    
    -- @Column(name = "pagos_pendientes")
    COUNT(DISTINCT CASE WHEN p.estado = 'PENDIENTE' THEN p.id END) AS pagos_pendientes,
    
    -- @Column(name = "pagos_fallidos")
    COUNT(DISTINCT CASE WHEN p.estado = 'FALLIDO' THEN p.id END) AS pagos_fallidos,

    -- @Column(name = "ingresos_totales")
    -- Sumar solo pagos COMPLETADOS (aunque la columna se llame pagos_exitosos)
    SUM(CASE WHEN p.estado = 'COMPLETADO' THEN p.monto ELSE 0 END) AS ingresos_totales,
    
    -- @Column(name = "ticket_promedio")
    AVG(CASE WHEN p.estado = 'COMPLETADO' THEN p.monto END) AS ticket_promedio,
    
    -- @Column(name = "ticket_maximo")
    MAX(CASE WHEN p.estado = 'COMPLETADO' THEN p.monto END) AS ticket_maximo,
    
    -- @Column(name = "ticket_minimo")
    MIN(CASE WHEN p.estado = 'COMPLETADO' THEN p.monto END) AS ticket_minimo,

    -- @Column(name = "pagos_tarjeta")
    COUNT(DISTINCT CASE WHEN p.metodo_pago = 'TARJETA' THEN p.id END) AS pagos_tarjeta,
    
    -- @Column(name = "pagos_yape")
    COUNT(DISTINCT CASE WHEN p.metodo_pago = 'YAPE' THEN p.id END) AS pagos_yape,
    
    -- @Column(name = "pagos_efectivo")
    COUNT(DISTINCT CASE WHEN p.metodo_pago = 'EFECTIVO' THEN p.id END) AS pagos_efectivo,

    -- @Column(name = "ingresos_tarjeta")
    SUM(CASE WHEN p.estado = 'COMPLETADO' AND p.metodo_pago = 'TARJETA' THEN p.monto ELSE 0 END) AS ingresos_tarjeta,
    
    -- @Column(name = "ingresos_yape")
    SUM(CASE WHEN p.estado = 'COMPLETADO' AND p.metodo_pago = 'YAPE' THEN p.monto ELSE 0 END) AS ingresos_yape,
    
    -- @Column(name = "ingresos_efectivo")
    SUM(CASE WHEN p.estado = 'COMPLETADO' AND p.metodo_pago = 'EFECTIVO' THEN p.monto ELSE 0 END) AS ingresos_efectivo,

    -- @Column(name = "clientes_unicos")
    COUNT(DISTINCT r.cliente_id) AS clientes_unicos,
    
    -- @Column(name = "servicios_distintos")
    COUNT(DISTINCT r.servicio_id) AS servicios_distintos,
    
    -- @Column(name = "bahias_utilizadas")
    COUNT(DISTINCT r.bahia_id) AS bahias_utilizadas,

    -- @Column(name = "descuentos_totales")
    SUM(r.descuento_aplicado) AS descuentos_totales,
    
    -- @Column(name = "reservas_con_cupon")
    COUNT(DISTINCT CASE WHEN r.cupon_id IS NOT NULL THEN r.id END) AS reservas_con_cupon,

    -- @Column(name = "duracion_promedio_servicio")
    AVG(s.duracion_minutos) AS duracion_promedio_servicio,

    -- @Column(name = "servicios_basico")
    COUNT(DISTINCT CASE WHEN s.tipo = 'BASICO' THEN r.id END) AS servicios_basico,
    
    -- @Column(name = "servicios_completo")
    COUNT(DISTINCT CASE WHEN s.tipo = 'COMPLETO' THEN r.id END) AS servicios_completo,
    
    -- @Column(name = "servicios_premium")
    COUNT(DISTINCT CASE WHEN s.tipo = 'PREMIUM' THEN r.id END) AS servicios_premium,
    
    -- @Column(name = "servicios_express")
    COUNT(DISTINCT CASE WHEN s.tipo = 'EXPRESS' THEN r.id END) AS servicios_express

FROM pagos p
INNER JOIN reservas r ON p.reserva_id = r.id
INNER JOIN servicios s ON r.servicio_id = s.id
GROUP BY DATE(p.fecha_pago)
ORDER BY fecha DESC;


-- ============================================
-- NOTAS IMPORTANTES
-- ============================================
--
-- 1. CORRECCIÓN APLICADA:
--    - Modelo Java VistaResumenPagosDiario TENÍA: @Column(name = "pagos_exitosos")
--    - Enum EstadoPago tiene: COMPLETADO (NO existe EXITOSO)
--    - SOLUCIÓN: Cambiar el modelo Java a "pagos_completados"
--    - SQL usa: pagos_completados (consistente con el enum)
--
-- 2. CAMPO "numero_transaccion":
--    - Modelo Pago tiene: transaccionId → transaccion_id en BD
--    - Modelo Vista espera: numero_transaccion
--    - SOLUCIÓN: Alias AS numero_transaccion
--
-- 3. TODAS LAS COLUMNAS coinciden EXACTAMENTE con los modelos @Entity corregidos
--
-- 4. Para ejecutar:
--    mysql -u root -p carwash_db < vistas_perfectas.sql
--
-- ============================================
