package com.fabricaescuela.models.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaqueteResponseDto {
    private Integer id;
    private String codigoPaquete;
    private String remitente;
    private String destinatario;
    private String descripcion; // Si luego agregas descripción en la entidad
}


