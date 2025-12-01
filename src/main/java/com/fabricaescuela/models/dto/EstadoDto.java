package com.fabricaescuela.models.dto;

import org.springframework.hateoas.RepresentationModel;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoDto extends RepresentationModel<EstadoDto> {
    private Long id;
    private String nombre;
}

