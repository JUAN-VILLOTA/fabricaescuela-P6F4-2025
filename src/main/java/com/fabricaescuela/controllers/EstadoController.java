package com.fabricaescuela.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fabricaescuela.models.entity.Estado;
import com.fabricaescuela.service.EstadoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/estados")
@Tag(name = "Estados", description = "Operaciones CRUD para los estados")
public class EstadoController {

    private final EstadoService estadoService;

    public EstadoController(EstadoService estadoService) {
        this.estadoService = estadoService;
    }

    @Operation(summary = "Obtener todos los estados", 
               description = "Retorna la lista de estados con enlaces HATEOAS")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Estado>>> getAllEstados() {
        List<Estado> estados = estadoService.findAll();
        
        // Convertir cada estado en EntityModel con enlaces
        List<EntityModel<Estado>> estadosConEnlaces = estados.stream()
            .map(estado -> {
                EntityModel<Estado> entityModel = EntityModel.of(estado);
                entityModel.add(linkTo(methodOn(EstadoController.class)
                    .getEstadoById(estado.getId())).withSelfRel());
                entityModel.add(linkTo(methodOn(EstadoController.class)
                    .updateEstado(estado.getId(), null)).withRel("actualizar"));
                entityModel.add(linkTo(methodOn(EstadoController.class)
                    .deleteEstado(estado.getId())).withRel("eliminar"));
                return entityModel;
            })
            .collect(Collectors.toList());
        
        // Crear CollectionModel con enlace self
        CollectionModel<EntityModel<Estado>> collectionModel = CollectionModel.of(estadosConEnlaces);
        collectionModel.add(linkTo(methodOn(EstadoController.class).getAllEstados()).withSelfRel());
        collectionModel.add(linkTo(methodOn(EstadoController.class).createEstado(null)).withRel("crear"));
        
        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Obtener un estado por ID",
               description = "Retorna un estado específico con enlaces HATEOAS")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Estado>> getEstadoById(@PathVariable Integer id) {
        return estadoService.findById(id)
                .map(estado -> {
                    EntityModel<Estado> entityModel = EntityModel.of(estado);
                    entityModel.add(linkTo(methodOn(EstadoController.class)
                        .getEstadoById(id)).withSelfRel());
                    entityModel.add(linkTo(methodOn(EstadoController.class)
                        .getAllEstados()).withRel("all-estados"));
                    entityModel.add(linkTo(methodOn(EstadoController.class)
                        .updateEstado(id, null)).withRel("actualizar"));
                    entityModel.add(linkTo(methodOn(EstadoController.class)
                        .deleteEstado(id)).withRel("eliminar"));
                    return ResponseEntity.ok(entityModel);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Crear un nuevo estado",
        description = """
            Crea un nuevo estado que puede ser asignado a los paquetes.
            
            **Campos requeridos:**
            - nombreEstado (string): Nombre del estado (max 30 caracteres)
            - descripcionEstado (string): Descripción del estado (max 255 caracteres)
            
            **Ejemplo de payload (JSON):**
            ```json
            {
              "nombreEstado": "En Ruta",
              "descripcionEstado": "El paquete está en camino hacia su destino"
            }
            ```
            
            **Respuestas:**
            - 200: Estado creado exitosamente
            - 400: Datos inválidos
            - 401: No autenticado
            """
    )
    @PostMapping
    public ResponseEntity<Estado> createEstado(@RequestBody Estado estado) {
        return ResponseEntity.ok(estadoService.save(estado));
    }

    @Operation(summary = "Actualizar un estado existente")
    @PutMapping("/{id}")
    public ResponseEntity<Estado> updateEstado(@PathVariable Integer id, @RequestBody Estado estado) {
        return estadoService.findById(id)
                .map(existing -> {
                    existing.setNombreEstado(estado.getNombreEstado());
                    existing.setDescripcionEstado(estado.getDescripcionEstado());
                    return ResponseEntity.ok(estadoService.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar un estado por ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEstado(@PathVariable Integer id) {
        return estadoService.findById(id)
                .map(existing -> {
                    estadoService.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
