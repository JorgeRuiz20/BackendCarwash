package com.carwash.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    private String token;
    private String tipo = "Bearer";
    private UsuarioDTO usuario;
}