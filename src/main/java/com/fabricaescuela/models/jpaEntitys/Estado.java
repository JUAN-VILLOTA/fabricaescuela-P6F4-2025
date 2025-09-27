package com.fabricaescuela.models.jpaEntitys;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "\"Estados\"")
public class Estado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idEstado\"", nullable = false)
    private Integer id;

    @Size(max = 30)
    @Column(name = "\"nombreEstado\"", length = 30)
    private String nombreEstado;

    @Size(max = 255)
    @Column(name = "\"descripcionEstado\"")
    private String descripcionEstado;

}