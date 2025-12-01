package com.fabricaescuela.models.dto;

import org.springframework.hateoas.RepresentationModel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaqueteResponseDto extends RepresentationModel<PaqueteResponseDto> {
    private Integer id;
    private String codigoPaquete;
    private String remitente;
    private String destinatario;
    private String destino;
    private String estadoActual;
    private String descripcion;
}


