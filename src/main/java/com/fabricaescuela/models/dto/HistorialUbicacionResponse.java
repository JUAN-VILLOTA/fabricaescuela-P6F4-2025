package com.fabricaescuela.models.dto;

import java.time.Instant;

import org.springframework.hateoas.RepresentationModel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HistorialUbicacionResponse extends RepresentationModel<HistorialUbicacionResponse> {
    private Integer id;
    private String codigoPaquete;
    private String ubicacion;
    private Instant fechaRegistro;
}
