package com.carwash.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bahias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bahia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    private String nombre;

    @Enumerated(EnumType.STRING)
    private EstadoBahia estado;

    @Builder.Default
    private Boolean disponible = true;

    @Builder.Default
    @OneToMany(mappedBy = "bahia")
    private Set<Reserva> reservas = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "bahia", cascade = CascadeType.ALL)
    private Set<SensorTelemetria> sensores = new HashSet<>();
}