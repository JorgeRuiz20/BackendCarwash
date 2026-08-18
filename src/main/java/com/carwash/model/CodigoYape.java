package com.carwash.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "codigos_yape")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodigoYape {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    @Size(min = 1, max = 5)
    private String codigo;

    @Column(nullable = false)
    private String numeroTelefono;

    private LocalDateTime fechaUso;

    @Column(nullable = false)
    @Builder.Default
    private Boolean usado = false;
}
