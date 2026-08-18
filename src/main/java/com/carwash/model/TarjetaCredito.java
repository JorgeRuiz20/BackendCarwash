package com.carwash.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tarjetas_credito")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarjetaCredito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numeroTarjeta; // 16 dígitos

    @Column(nullable = false)
    private String cvv; // 3 dígitos

    @Column(nullable = false)
    private String fechaExpiracion; // MM/YY

    private String nombreTitular;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;
}
