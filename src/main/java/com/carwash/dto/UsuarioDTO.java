package com.carwash.dto;

import com.carwash.model.TipoUsuario;
import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {
    private Long id;
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    private String email;
    
    @NotBlank(message = "El nombre es requerido")
    private String nombre;
    
    private String apellido;
    private String telefono;
    private TipoUsuario tipo;
    private Boolean activo;
    private PerfilClienteDTO perfilCliente;
}