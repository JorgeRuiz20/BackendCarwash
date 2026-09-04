# 🚗 Carwash Solutions Backend

> **Sistema Integral de Gestión, Automatización IoT y Fidelización para Centros de Lavado de Vehículos**

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/Security-JWT%20Stateless-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![OpenAPI](https://img.shields.io/badge/Swagger-OpenAPI%203.0-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](https://swagger.io/)

---

## 📌 1. Visión General del Proyecto

**Carwash Solutions Backend** es una plataforma robusta y escalable desarrollada sobre **Spring Boot 3** y **Java 17**, diseñada para digitalizar, controlar y automatizar integralmente las operaciones de un autolavado moderno.

El sistema unifica en una sola solución:
- **Gestión operativa en tiempo real:** Control de bahías de lavado, catálogo de servicios y estados de reservas.
- **Seguridad perimetral:** Autenticación con tokens JWT, arquitectura sin estado (*stateless*), control de acceso basado en roles (RBAC) y filtro de mitigación de ataques de fuerza bruta (*Rate Limiting* por IP).
- **Procesamiento de pagos y programa de fidelización:** Soporte para tarjetas, billeteras digitales (Yape / Plin) y efectivo, con acumulación automática de puntos de fidelidad canjeables por cupones de descuento dinámicos.
- **Simulación y telemetría IoT industrial:** Monitoreo automatizado en segundo plano de sensores críticos (presión de agua, nivel de tanque, temperatura, flujo y energía), con un protocolo de **parada de emergencia automática** ante anomalías operativas.
- **Inteligencia de negocio y reportes:** Vistas SQL nativas de alto rendimiento, cálculo de métricas financieras y generación de reportes ejecutivos en formato **PDF** con diseño corporativo (iText).
- **Alta disponibilidad en la nube:** Preparado para despliegue en contenedores Docker (Render / Aiven Cloud) con endpoints de verificación (*health checks*) y soporte para servicios de monitoreo tipo UptimeRobot.

---

## 🏗️ 2. Arquitectura del Sistema

El backend implementa una arquitectura en capas desacoplada (**N-Tier Layered Architecture**) que promueve la separación de responsabilidades, mantenibilidad y facilidad de prueba.

```
                  ┌────────────────────────────────────────┐
                  │          Cliente Web / Móvil           │
                  │   (React / Next.js / Frontend App)     │
                  └──────────────────┬─────────────────────┘
                                     │ HTTP/REST (JSON) + Bearer JWT
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                           CAPA DE SEGURIDAD                              │
│  ┌─────────────────────────┐            ┌─────────────────────────────┐  │
│  │   RateLimitingFilter    │ ─────────► │    JwtAuthenticationFilter  │  │
│  │ (Anti Fuerza Bruta IP)  │            │    (Validación de Claims)   │  │
│  └─────────────────────────┘            └──────────────┬──────────────┘  │
└────────────────────────────────────────────────────────┼─────────────────┘
                                                         │
                                                         ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                         CAPA DE CONTROLADORES                            │
│  AuthController │ ReservaController │ PagoController │ BahiaController   │
│  ServicioController │ CuponController │ FidelizacionController           │
│  SensorTelemetriaController │ AlertaController │ ReporteController       │
│  DashboardController │ HealthController                                  │
└────────────────────────────────────────┬─────────────────────────────────┘
                                         │ DTOs (Data Transfer Objects)
                                         ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                           CAPA DE SERVICIOS                              │
│  Lógica de Negocio │ Transacciones (@Transactional) │ Validación         │
│  TelemetriaAutomaticaService (Simulador IoT @Scheduled 15s)              │
│  CuponScheduledTasks (Desactivación cron 2:00 AM)                        │
│  PdfService (Generación iText) │ ModelMapper Mapping                     │
└────────────────────────────────────────┬─────────────────────────────────┘
                                         │
                                         ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                   CAPA DE ACCESO A DATOS (JPA / SQL)                     │
│  Spring Data JPA Repositories │ Hibernate ORM │ Vistas SQL Personalizadas│
└────────────────────────────────────────┬─────────────────────────────────┘
                                         │ JDBC / HikariCP Pool
                                         ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                       BASE DE DATOS RELACIONAL                           │
│                 MySQL 8.0 (Instancia Cloud Aiven)                        │
│   Tablas Maestras │ Tablas Operativas │ Vistas Analíticas de Rendimiento │
└──────────────────────────────────────────────────────────────────────────┘
```

### Principios Arquitecturales Clave
1. **Desacoplamiento DTO / Entidad:** Las entidades JPA nunca se exponen directamente hacia los clientes HTTP; se transforman mediante `ModelMapper` protegiendo la estructura interna de la base de datos.
2. **Respuesta Unificada (`ApiResponse<T>`):** Toda respuesta JSON posee un formato estandarizado que incluye `success`, `message`, `data` y `timestamp`.
3. **Manejo Centralizado de Excepciones:** `GlobalExceptionHandler` (`@RestControllerAdvice`) captura errores de validación, reglas de negocio (`BusinessException`), recursos no encontrados (`ResourceNotFoundException`) y fallos de autenticación.
4. **Circuito de Seguridad IoT (Fail-Safe Mechanism):** En caso de lecturas anómalas de telemetría, el sistema altera el estado del hardware y la reserva de forma autónoma sin intervención manual inmediata.

---

## 🌟 3. Módulos y Funcionalidades Principales

### 3.1. Autenticación, Seguridad y Rate Limiting
- **Autenticación con JWT:** Tokens criptográficos firmados con HMAC-SHA256 que encapsulan email y roles del usuario con vigencia configurable.
- **Protección contra Fuerza Bruta (`RateLimitingFilter`):**
  - Aplica exclusivamente a las peticiones `POST /api/auth/login`.
  - Mantiene un registro por dirección IP (compatible con proxy inverso y header `X-Forwarded-For`).
  - Bloquea temporalmente las solicitudes de una IP por **15 minutos** tras **5 intentos fallidos consecutivos**, retornando código HTTP `429 Too Many Requests`.
- **Control de Acceso Basado en Roles (RBAC):**
  - `ADMINISTRADOR`: Control total sobre usuarios, tarifas de servicios, bahías, cupones promocionales y auditorías.
  - `OPERADOR`: Operaciones diarias, supervisión del dashboard, inicio/finalización de lavados y resolución de alertas de telemetría.
  - `CLIENTE`: Consulta pública de catálogo, creación de reservas propias, realización de pagos y acceso al programa de fidelización.
- **CORS Configurado:** Permite orígenes específicos en desarrollo (`localhost:5173`, `localhost:3000`) y en producción (`https://frontendcarwash.onrender.com`), asegurando envío de credenciales (`allowCredentials=true`).

---

### 3.2. Gestión de Bahías de Lavado (Bays)
- Control del ciclo de vida físico de cada box de lavado:
  - `DISPONIBLE`: Bahía lista para recibir un vehículo.
  - `OCUPADA`: Bahía reservada con un turno confirmado próximo a iniciar.
  - `EN_USO`: Bahía en ejecución activa de lavado (emitiendo telemetría continua).
  - `MANTENIMIENTO`: Bahía inhabilitada por avería o bloqueada automáticamente por alerta crítica del sistema IoT.
- Consulta rápida en tiempo real de bahías disponibles y contadores para la asignación de turnos.

---

### 3.3. Catálogo de Servicios
- Clasificación de servicios por tipo (`TipoServicio`):
  - `BASICO`: Lavado exterior completo.
  - `COMPLETO`: Lavado exterior, interior, aspirado y limpieza de tapices.
  - `PREMIUM`: Lavado completo, encerado, pulido y aromatización de habitáculo.
  - `EXPRESS`: Lavado exterior rápido de alta rotación.
- Atributos gestionables: Nombre, descripción detallada, precio en Soles (PEN), duración estimada en minutos y estado activo/inactivo.

---

### 3.4. Ciclo de Vida de Reservas
- **Flujo de Estados:**
  ```
  [CREADA: PENDIENTE]
          │
          │ (Pago exitoso procesado)
          ▼
    [CONFIRMADA]
          │
          │ (Operador inicia el servicio en bahía)
          ▼
    [EN_PROGRESO] ──── (Alerta crítica sensor) ────► [EN_ESPERA (Detenido)]
          │                                                  │
          │ (Lavado finalizado)                              │ (Operador resuelve alerta)
          ▼                                                  ▼
    [COMPLETADA]                                    [EN_PROGRESO (Reanudado)]
  ```
- Asociación automática de la reserva al usuario autenticado extraído de los *claims* del JWT.
- Integración con cupones de descuento, descontando el monto porcentual o fijo del valor final.

---

### 3.5. Pasarela y Métodos de Pago
- Soporte para múltiples formas de pago (`MetodoPago`):
  - **Tarjeta de Crédito / Débito:** Validación simulada contra base de datos de tarjetas autorizadas (número, fecha de expiración y CVV).
  - **Billeteras Móviles (Yape / Plin):** Validación de número de teléfono y código de confirmación de 6 dígitos.
  - **Efectivo:** Cobro directo en estación.
- Generación de identificador único de transacción (`transaccionId`) mediante `UUID v4`.
- **Cierre y acreditación inmediata:** Al completarse el pago, la reserva pasa automáticamente a `CONFIRMADA` y se acreditan los puntos de fidelidad correspondientes.

---

### 3.6. Programa de Fidelización y Motor de Cupones
- **Regla de Puntos:** Por cada **S/ 1.00 gastado**, el cliente acumula **10 puntos de fidelidad** en su perfil (`PerfilCliente`).
- **Niveles de Canje de Puntos:**
  - 🟢 **100 Puntos** ➔ Cupón de **5% de descuento**
  - 🔵 **200 Puntos** ➔ Cupón de **10% de descuento**
  - 🟣 **500 Puntos** ➔ Cupón de **20% de descuento**
- **Emisión de Cupones Inteligentes:**
  - Código único generado algorítmicamente: `FIDE-[PCT]PCT-[YYYYMMDD]-[RANDOM4D]`.
  - Propiedades: Un solo uso (`usosMaximos=1`), vigencia de 30 días, asociado estrictamente al perfil del cliente que lo canjeó.
- **Cupones Promocionales (Admin):** Cupones corporativos con fecha de vigencia, tipo de descuento (porcentaje o monto fijo) y límite global de redenciones.
- **Limpieza Programada:** Tarea programada `@Scheduled` que se ejecuta diariamente a las **2:00 AM** para desactivar automáticamente los cupones cuya fecha de expiración haya pasado.

---

### 3.7. Telemetría IoT y Parada de Emergencia Automatizada
El sistema cuenta con un motor autónomo de telemetría (`TelemetriaAutomaticaService`) que evalúa constantemente el estado de las instalaciones:
- **Simulación en Tiempo Real:** Cada **15 segundos**, se generan lecturas paramétricas para todas las bahías que se encuentren en estado `EN_USO`.
- **Parámetros Monitoreados:**
  - 💧 **Nivel de Agua:** Normal (50–100%). Advertencia (< 30%). Crítico (< 20%).
  - ⏱️ **Presión de Agua:** Normal (2.0–5.0 bar). Advertencia (< 2.0 o > 5.0 bar). Crítico (< 1.5 o > 5.5 bar).
  - 🌡️ **Temperatura:** Normal (20–45 °C). Advertencia (> 45 °C). Crítico (> 50 °C).
  - 🌊 **Flujo de Agua:** Normal (35–80 L/min). Advertencia (< 35 L/min). Crítico (< 25 L/min).
  - ⚡ **Consumo de Energía:** Monitoreo continuo en kWh.
- **Protocolo de Parada de Emergencia:**
  1. Si un sensor dispara una lectura clasificada como **`CRITICA`**:
  2. La bahía pasa de inmediato a estado **`MANTENIMIENTO`** (`disponible = false`).
  3. La reserva activa en esa bahía pasa automáticamente a estado **`EN_ESPERA`**, registrando la causa exacta de la detención en sus observaciones.
  4. La generación de telemetría para esa bahía queda **suspendida preventivamente** para evitar saturación de lecturas anómalas.
- **Panel de Intervención del Operador:**
  - Los operadores visualizan las alertas activas y críticas.
  - Endpoint de resolución (`POST /api/alertas/{id}/resolver`) con comentarios técnicos obligatorios y bandera para **reanudar automáticamente el servicio**, devolviendo la bahía a `EN_USO` y la reserva a `EN_PROGRESO`.

---

### 3.8. Vistas SQL y Exportación de Reportes a PDF
Para optimizar el rendimiento y evitar cálculos pesados en memoria, el sistema utiliza vistas SQL nativas inicializadas en `src/main/resources/db/views.sql`:
1. `vista_reporte_alertas_bahias`: Agrupación de alertas críticas, advertencias y promedios de telemetría de los últimos 7 días por cada bahía.
2. `vista_reporte_pagos`: Detalle relacional consolidado entre pago, cliente, vehículo, servicio, cupones y duración efectiva del lavado.
3. `vista_resumen_pagos_diario`: Cierre diario agrupado por fecha con ingresos totales, ticket promedio, ticket máximo/mínimo y desglose por método de pago (Tarjeta, Yape, Efectivo).

#### Generación de Documentos PDF (`PdfService` - iText):
- Genera archivos PDF profesionales con tipografías Helvetica estilizadas, paleta de colores corporativa (azul/verde/naranja/rojo), tablas estructuradas y pie de página con numeración:
  - 📄 **Reporte Diario en PDF:** Resumen de ingresos, conteo de servicios y rendimiento por fecha (`/api/reportes/pdf/diario`).
  - 📄 **Reporte Mensual en PDF:** Agregado consolidado para balances mensuales (`/api/reportes/pdf/mensual`).
  - 📄 **Reporte de Pagos en PDF:** Detalle pormenorizado de transacciones por rango de fechas (`/api/reportes/pdf/pagos`).
  - 📄 **Reporte de Alertas en PDF:** Informe de auditoría operativa de sensores e incidencias técnicas por bahía (`/api/reportes/pdf/alertas`).

---

### 3.9. Dashboard de Mando Operativo
- Endpoint de alto nivel (`/api/dashboard`) que compila en una sola llamada atómica:
  - Reservas del día de hoy.
  - Reservas pendientes de confirmación.
  - Reservas en ejecución activa (`EN_PROGRESO`).
  - Ingresos del día e ingresos del mes acumulados.
  - Bahías disponibles al instante.
  - Listado de las próximas reservas agendadas.
  - Listado de alertas técnicas no resueltas.

---

### 3.10. Health Check y Mantenimiento Activo en Cloud
- Rutas públicas `/health`, `/api/health`, `/health/ping` y `/` que retornan estado `OK` con timestamp.
- Diseñado para integración con herramientas como **UptimeRobot** o monitores de salud, evitando que los contenedores en planes gratuitos de Render entren en suspensión (*cold start*).

---

## 💻 4. Stack Tecnológico

| Capa / Componente | Tecnología | Versión | Propósito |
| :--- | :--- | :--- | :--- |
| **Lenguaje** | Java OpenJDK | 17 (LTS) | Lenguaje base del proyecto |
| **Framework** | Spring Boot | 3.2.0 | Core del framework backend |
| **Seguridad** | Spring Security | 6.x | Control de acceso, filtros de seguridad y RBAC |
| **Criptografía / Auth** | JJWT (Java JWT) | 0.11.5 | Creación y validación de tokens JWT |
| **Acceso a Datos** | Spring Data JPA / Hibernate | 3.2.0 | Mapeo objeto-relacional y consultas optimizadas |
| **Base de Datos** | MySQL | 8.0+ | Persistencia relacional alojada en Aiven Cloud |
| **Connection Pool** | HikariCP | Integrado | Pool de conexiones a base de datos de alta velocidad |
| **Generación de PDFs** | iText PDF | 5.5.13.3 | Renderizado de reportes ejecutivos en PDF |
| **Mapeo de Objetos** | ModelMapper | 3.1.1 | Conversión automática entre Entidades y DTOs |
| **Documentación** | SpringDoc OpenAPI | 2.2.0 | Documentación interactiva Swagger UI |
| **Utilidades de Código** | Project Lombok | Integrado | Reducción de código boilerplate (Getters, Setters, Builders) |
| **Contenedorización** | Docker | Multi-Stage | Empaquetado ligero para producción (Alpine JRE 17) |

---

## 📁 5. Estructura del Proyecto

```
carwash-backend/
├── src/
│   ├── main/
│   │   ├── java/com/carwash/
│   │   │   ├── CarwashApplication.java        # Punto de entrada de la aplicación Spring Boot
│   │   │   ├── config/                        # Configuraciones generales
│   │   │   │   ├── DataInitializer.java      # Carga de datos semilla (admin, clientes, bahías, servicios)
│   │   │   │   ├── ModelMapperConfig.java    # Bean de configuración para ModelMapper
│   │   │   │   ├── OpenApiConfig.java        # Especificación y metadatos Swagger / OpenAPI 3
│   │   │   │   └── SchedulingConfig.java     # Habilitación de tareas programadas (@EnableScheduling)
│   │   │   ├── controller/                    # Endpoints REST de la API
│   │   │   │   ├── AlertaController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── BahiaController.java
│   │   │   │   ├── CuponController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── FidelizacionController.java
│   │   │   │   ├── PagoController.java
│   │   │   │   ├── ReporteController.java
│   │   │   │   ├── ReservaController.java
│   │   │   │   ├── SensorTelemetriaController.java
│   │   │   │   ├── ServicioController.java
│   │   │   │   └── UsuarioController.java
│   │   │   ├── dto/                           # Data Transfer Objects y ApiResponse genérico
│   │   │   ├── exception/                     # Excepciones de negocio y GlobalExceptionHandler
│   │   │   ├── health/                        # HealthController para Render y UptimeRobot
│   │   │   ├── model/                         # Entidades JPA y Enums de dominio
│   │   │   │   └── view/                      # Entidades mapeadas a Vistas SQL de reportes
│   │   │   ├── repository/                    # Repositorios Spring Data JPA
│   │   │   │   └── view/                      # Repositorios para las Vistas SQL
│   │   │   ├── scheduled/                     # Tareas cron (limpieza de cupones expirados)
│   │   │   ├── security/                      # JWT, UserDetails, Rate Limiting y SecurityConfig
│   │   │   └── service/                       # Servicios de lógica de negocio, PDFs y Telemetría
│   │   └── resources/
│   │       ├── db/
│   │       │   └── views.sql                  # Definiciones DDL de vistas analíticas SQL
│   │       └── application.properties         # Configuración del entorno, base de datos y seguridad
│   └── test/                                  # Pruebas unitarias y de integración
├── Dockerfile                                 # Multi-stage Dockerfile para compilación y despliegue
├── pom.xml                                    # Dependencias de Maven y configuración de build
└── README.md                                  # Documentación integral del proyecto
```

---

## 📡 6. Catálogo de Endpoints de la API

### 🔐 Autenticación (`/api/auth`)
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Público | Registro de nuevos clientes |
| `POST` | `/api/auth/login` | Público (*Rate Limited*) | Autenticación y obtención de token JWT |
| `GET` | `/api/auth/validate` | Autenticado | Verificación de validez del token actual |

### 🚗 Bahías de Lavado (`/api/bahias`)
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/bahias` | Autenticado | Listado completo de bahías |
| `GET` | `/api/bahias/disponibles` | Público | Listado de bahías listas para uso |
| `GET` | `/api/bahias/count/disponibles` | Público | Cantidad numérica de bahías libres |
| `GET` | `/api/bahias/{id}` | Autenticado | Detalle de una bahía por su ID |
| `POST` | `/api/bahias` | `ADMINISTRADOR` | Creación de una nueva bahía |
| `PATCH`| `/api/bahias/{id}/estado` | `OPERADOR`, `ADMIN` | Cambio de estado de una bahía |

### 🛠️ Servicios (`/api/servicios`)
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/servicios` | Autenticado | Listar todos los servicios |
| `GET` | `/api/servicios/activos` | Público | Listar servicios habilitados |
| `GET` | `/api/servicios/{id}` | Público | Detalle de un servicio específico |
| `GET` | `/api/servicios/tipo/{tipo}` | Autenticado | Filtrar servicios por tipo (`BASICO`, `PREMIUM`, etc.) |
| `POST` | `/api/servicios` | `ADMINISTRADOR` | Crear un nuevo servicio |
| `PUT` | `/api/servicios/{id}` | `ADMINISTRADOR` | Actualizar precio o información del servicio |
| `DELETE`| `/api/servicios/{id}` | `ADMINISTRADOR` | Inhabilitar o eliminar un servicio |

### 📅 Reservas (`/api/reservas`)
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/reservas` | `CLIENTE`, `ADMIN` | Crear nueva reserva (toma cliente de JWT) |
| `GET` | `/api/reservas/mis-reservas` | `CLIENTE`, `ADMIN` | Listar reservas del usuario en sesión |
| `GET` | `/api/reservas/hoy` | `OPERADOR`, `ADMIN` | Listado de reservas programadas para hoy |
| `GET` | `/api/reservas/estado/{estado}` | `OPERADOR`, `ADMIN` | Filtrar reservas por estado |
| `PATCH`| `/api/reservas/{id}/iniciar` | `OPERADOR`, `ADMIN` | Iniciar lavado y asociarlo a una bahía |
| `PATCH`| `/api/reservas/{id}/estado` | `OPERADOR`, `ADMIN` | Modificar estado de la reserva |
| `DELETE`| `/api/reservas/{id}` | Autenticado | Cancelar una reserva |

### 💳 Pagos (`/api/pagos`)
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/pagos` | `CLIENTE`, `ADMIN` | Procesar pago de una reserva y acreditar puntos |
| `GET` | `/api/pagos/{id}` | Autenticado | Obtener comprobante/detalle de pago |
| `GET` | `/api/pagos/reserva/{reservaId}` | Autenticado | Obtener pago asociado a una reserva |
| `GET` | `/api/pagos` | `OPERADOR`, `ADMIN` | Listar todas las transacciones |
| `GET` | `/api/pagos/ingresos/hoy` | `OPERADOR`, `ADMIN` | Calcular total de ingresos del día |
| `GET` | `/api/pagos/ingresos/mes` | `OPERADOR`, `ADMIN` | Calcular total de ingresos del mes actual |

### 🎁 Fidelización de Clientes (`/api/fidelizacion`)
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/fidelizacion/canjear` | `CLIENTE` | Canjear puntos por cupones de 5%, 10% o 20% |
| `GET` | `/api/fidelizacion/info` | `CLIENTE` | Consulta de saldo de puntos y opciones de canje |
| `GET` | `/api/fidelizacion/mis-cupones` | `CLIENTE` | Historial de cupones canjeados |
| `GET` | `/api/fidelizacion/cupones-activos` | `CLIENTE` | Cupones disponibles y vigentes para uso |
| `GET` | `/api/fidelizacion/validar-cupon/{codigo}` | `CLIENTE` | Validar pertenencia y vigencia de cupón |

### 🏷️ Cupones Promocionales (`/api/cupones`)
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/cupones/validar?codigo=XYZ` | Público | Validar código de cupón al reservar |
| `GET` | `/api/cupones/todos` | `ADMINISTRADOR` | Listar todos los cupones existentes |
| `GET` | `/api/cupones` | `ADMINISTRADOR` | Listar solo cupones activos |
| `POST` | `/api/cupones` | `ADMINISTRADOR` | Crear cupón promocional |
| `DELETE`| `/api/cupones/{id}` | `ADMINISTRADOR` | Desactivar cupón |

### 🚨 Telemetría IoT y Alertas (`/api/telemetria` & `/api/alertas`)
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/alertas/activas` | `OPERADOR`, `ADMIN` | Ver todas las alertas técnicas pendientes |
| `GET` | `/api/alertas/criticas` | `OPERADOR`, `ADMIN` | Ver alertas con servicio detenido |
| `POST` | `/api/alertas/{id}/resolver` | `OPERADOR`, `ADMIN` | Resolver alerta y opcionalmente reanudar lavado |
| `GET` | `/api/telemetria/bahia/{bahiaId}` | `OPERADOR`, `ADMIN` | Historial de lecturas de sensores de una bahía |
| `GET` | `/api/telemetria/consumo-agua/promedio` | `OPERADOR`, `ADMIN` | Métricas de consumo hídrico por período |

### 📊 Reportes y Exportación PDF (`/api/reportes`)
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/reportes/diario?fecha=YYYY-MM-DD` | `OPERADOR`, `ADMIN` | Generar datos de reporte diario |
| `POST` | `/api/reportes/mensual?mes=M&anio=YYYY` | `OPERADOR`, `ADMIN` | Generar datos de reporte mensual |
| `GET` | `/api/reportes/pdf/diario?fecha=YYYY-MM-DD` | `OPERADOR`, `ADMIN` | 📥 Descargar Reporte Diario en PDF |
| `GET` | `/api/reportes/pdf/mensual?mes=M&anio=YYYY` | `OPERADOR`, `ADMIN` | 📥 Descargar Reporte Mensual en PDF |
| `GET` | `/api/reportes/pdf/pagos?fechaInicio=...&fechaFin=...` | `OPERADOR`, `ADMIN` | 📥 Descargar Reporte de Pagos en PDF |
| `GET` | `/api/reportes/pdf/alertas` | `OPERADOR`, `ADMIN` | 📥 Descargar Reporte de Alertas en PDF |

### 📈 Dashboard y Monitoreo (`/api/dashboard` & `/health`)
| Método | Endpoint | Acceso | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/dashboard` | `OPERADOR`, `ADMIN` | Resumen general de KPIs en tiempo real |
| `GET` | `/health`, `/api/health`, `/` | Público | Endpoint de estado para Render / UptimeRobot |
| `GET` | `/health/ping` | Público | Verificación simple en texto plano ("OK") |

---

## ⚙️ 7. Variables de Entorno y Configuración

El proyecto se configura mediante propiedades de Spring en `application.properties` con soporte completo de variables de entorno:

| Variable de Entorno | Valor por Defecto | Descripción |
| :--- | :--- | :--- |
| `PORT` | `8080` | Puerto HTTP donde se enlaza el servidor embebido |
| `SPRING_DATASOURCE_URL` | *Aiven Cloud MySQL Connection String* | Cadena JDBC de conexión a MySQL |
| `SPRING_DATASOURCE_USERNAME` | `avnadmin` | Usuario de la base de datos |
| `SPRING_DATASOURCE_PASSWORD` | *(secreto)* | Contraseña de la base de datos |
| `JWT_SECRET` | *(clave de 256 bits predeterminada)* | Llave secreta para la firma y verificación de tokens JWT |
| `JWT_EXPIRATION` | `86400000` *(24 horas en ms)* | Tiempo de expiración de los tokens de sesión |
| `CORS_ORIGINS` | `https://frontendcarwash.onrender.com,http://localhost:5173,http://localhost:3000` | Orígenes web autorizados para interactuar con la API |

---

## 👥 8. Datos Semilla para Pruebas (`DataInitializer`)

Al iniciar la aplicación por primera vez (si la base de datos se encuentra vacía), el componente `DataInitializer` provisiona automáticamente los siguientes registros:

### Usuarios y Credenciales:
| Rol | Correo Electrónico | Contraseña | Teléfono |
| :--- | :--- | :--- | :--- |
| **Administrador** | `admin@carwash.com` | `admin123` | `999999999` |
| **Operador** | `operador@carwash.com` | `operador123` | `988888888` |
| **Cliente de Prueba** | `cliente@test.com` | `cliente123` | `977777777` |
| **Cliente Adicional 1** | `maria@test.com` | `maria123` | `977777777` |
| **Cliente Adicional 2** | `pedro@test.com` | `pedro123` | `977777777` |
| **Cliente Adicional 3** | `ana@test.com` | `ana123` | `977777777` |

### Bahías Iniciales:
- `B1` (Bahía 1), `B2` (Bahía 2), `B3` (Bahía 3), `B4` (Bahía 4) — Todas en estado `DISPONIBLE`.

### Servicios Iniciales:
- **Lavado Básico:** S/ 25.00 (20 min)
- **Lavado Completo:** S/ 45.00 (40 min)
- **Lavado Premium:** S/ 75.00 (60 min)
- **Lavado Express:** S/ 15.00 (10 min)

### Tarjetas de Prueba Habilitadas:
- `4532015112830366` (CVV: `123`, Exp: `12/25`, Titular: *Juan Pérez*)
- `5425233430109903` (CVV: `456`, Exp: `06/26`, Titular: *María García*)
- `378282246310005` (CVV: `789`, Exp: `09/27`, Titular: *Pedro Rodríguez*)

---

## 🚀 9. Guía de Ejecución Local y Despliegue

### Requisitos Previos
- **Java Development Kit (JDK):** Versión 17 o superior.
- **Acceso a MySQL:** Instancia local o remota (Aiven Cloud).
- **Git:** Para control de versiones.

### Ejecución Local con Maven Wrapper
```bash
# 1. Clonar el repositorio
git clone https://github.com/JorgeRuiz20/BackendCarwash.git
cd BackendCarwash

# 2. Configurar variables de entorno (PowerShell en Windows)
$env:SPRING_DATASOURCE_PASSWORD="tu_contraseña_aiven"
$env:JWT_SECRET="tu_clave_secreta_jwt_minimo_256_bits"

# 3. Compilar y ejecutar la aplicación
.\mvnw.cmd spring-boot:run
```

La aplicación quedará disponible en: `http://localhost:8080`
- **Documentación Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **Especificación OpenAPI (JSON):** `http://localhost:8080/v3/api-docs`
- **Health Check:** `http://localhost:8080/health`

---

### Despliegue con Docker
El proyecto incluye un `Dockerfile` optimizado con compilación multietapa (*Multi-stage build*):

```bash
# Construir la imagen Docker
docker build -t carwash-backend:latest .

# Ejecutar el contenedor mapeando el puerto 8080
docker run -d -p 8080:8080 \
  -e SPRING_DATASOURCE_PASSWORD="tu_password" \
  -e JWT_SECRET="tu_jwt_secret" \
  --name carwash-backend-app carwash-backend:latest
```

---

## 📄 10. Licencia y Créditos

- **Proyecto:** Carwash Solutions Backend
- **Autor / Repositorio:** [JorgeRuiz20/BackendCarwash](https://github.com/JorgeRuiz20/BackendCarwash)
- **Estado:** Producción / Despliegue Activo en Render
