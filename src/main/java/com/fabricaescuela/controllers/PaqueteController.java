package com.fabricaescuela.controllers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fabricaescuela.models.dto.PaqueteDireccionUpdateRequest;
import com.fabricaescuela.models.dto.PaqueteResponseDto;
import com.fabricaescuela.models.entity.Paquete;
import com.fabricaescuela.service.PaqueteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/paquetes")
@Tag(name = "Paquetes", description = "API para gestión de paquetes")
public class PaqueteController {

    private final PaqueteService paqueteService;

    public PaqueteController(PaqueteService paqueteService) {
        this.paqueteService = paqueteService;
    }

    @Operation(summary = "Consultar todos los paquetes",
            description = "Retorna la lista de paquetes con su estado actual y enlaces HATEOAS")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<PaqueteResponseDto>>> obtenerTodos() {
        List<PaqueteResponseDto> paquetes = paqueteService.obtenerTodos();
        
        // Convertir cada DTO en EntityModel con sus propios enlaces
        List<EntityModel<PaqueteResponseDto>> paquetesConEnlaces = paquetes.stream()
            .map(paquete -> {
                EntityModel<PaqueteResponseDto> entityModel = EntityModel.of(paquete);
                // Agregar enlace "self" a cada paquete individual
                entityModel.add(linkTo(methodOn(PaqueteController.class)
                    .consultarPorCodigo(paquete.getCodigoPaquete())).withSelfRel());
                // Agregar enlace para actualizar dirección
                entityModel.add(linkTo(methodOn(PaqueteController.class)
                    .actualizarDireccionDestino(paquete.getCodigoPaquete(), null)).withRel("actualizar-direccion"));
                return entityModel;
            })
            .collect(Collectors.toList());
        
        // Crear CollectionModel con enlace "self" al listado completo
        CollectionModel<EntityModel<PaqueteResponseDto>> collectionModel = CollectionModel.of(paquetesConEnlaces);
        collectionModel.add(linkTo(methodOn(PaqueteController.class).obtenerTodos()).withSelfRel());
        collectionModel.add(linkTo(methodOn(PaqueteController.class).obtenerPaquetesEnTransito()).withRel("en-transito"));
        
        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Consultar paquete por código",
            description = "Devuelve información detallada del paquete con enlaces HATEOAS de navegación")
    @GetMapping("/{codigo}")
    public ResponseEntity<?> consultarPorCodigo(@PathVariable String codigo) {
        Optional<PaqueteResponseDto> paqueteOpt = paqueteService.consultarPorCodigo(codigo);
        
        if (paqueteOpt.isPresent()) {
            PaqueteResponseDto paquete = paqueteOpt.get();
            EntityModel<PaqueteResponseDto> entityModel = EntityModel.of(paquete);
            
            // Agregar enlace "self" - al mismo recurso
            entityModel.add(linkTo(methodOn(PaqueteController.class)
                .consultarPorCodigo(codigo)).withSelfRel());
            
            // Agregar enlace "all-paquetes" - volver al listado completo
            entityModel.add(linkTo(methodOn(PaqueteController.class)
                .obtenerTodos()).withRel("all-paquetes"));
            
            // Agregar enlace "actualizar-direccion"
            entityModel.add(linkTo(methodOn(PaqueteController.class)
                .actualizarDireccionDestino(codigo, null)).withRel("actualizar-direccion"));
            
            // Agregar enlace "actualizar-estado"
            entityModel.add(linkTo(methodOn(PaqueteController.class)
                .actualizarEstado(codigo, null)).withRel("actualizar-estado"));
            
            // Enlace condicional "en-transito" solo si el estado es EN_TRANSITO
            if (paquete.getEstadoActual() != null && 
                paquete.getEstadoActual().contains("TRANSITO")) {
                entityModel.add(linkTo(methodOn(PaqueteController.class)
                    .obtenerPaquetesEnTransito()).withRel("paquetes-en-transito"));
            }
            
            return ResponseEntity.ok(entityModel);
        } else {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Paquete no encontrado");
            error.put("detalle", "No existe un paquete con el código: " + codigo);
            error.put("codigo", codigo);
            error.put("sugerencia", "Verifique el código del paquete e intente nuevamente");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(summary = "Consultar paquete en ruta por código",
        description = "Devuelve la información del paquete con enlaces HATEOAS si está en estado en ruta")
    @GetMapping("/en-ruta/{codigo}")
    public ResponseEntity<?> consultarEnRuta(@PathVariable String codigo) {
        Optional<PaqueteResponseDto> paqueteOpt = paqueteService.consultarEnRutaPorCodigo(codigo);
        
        if (paqueteOpt.isPresent()) {
            PaqueteResponseDto paquete = paqueteOpt.get();
            EntityModel<PaqueteResponseDto> entityModel = EntityModel.of(paquete);
            
            // Agregar enlaces HATEOAS
            entityModel.add(linkTo(methodOn(PaqueteController.class)
                .consultarEnRuta(codigo)).withSelfRel());
            entityModel.add(linkTo(methodOn(PaqueteController.class)
                .consultarPorCodigo(codigo)).withRel("detalle-completo"));
            entityModel.add(linkTo(methodOn(PaqueteController.class)
                .actualizarDireccionDestino(codigo, null)).withRel("actualizar-direccion"));
            entityModel.add(linkTo(methodOn(PaqueteController.class)
                .obtenerTodos()).withRel("all-paquetes"));
            
            return ResponseEntity.ok(entityModel);
        } else {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Paquete no encontrado o no está en ruta");
            error.put("detalle", "El paquete con código '" + codigo + "' no existe o no está en estado EN_RUTA");
            error.put("codigo", codigo);
            error.put("sugerencia", "Verifique que el paquete exista y esté en estado 'En Ruta'");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(
        summary = "Buscar paquetes por múltiples criterios",
        description = """
            Permite buscar paquetes usando diferentes criterios de búsqueda.
            Todos los parámetros son opcionales y se pueden combinar.
            
            **Ejemplos de uso:**
            - Buscar por código: `/api/paquetes/buscar?codigoPaquete=PKG-12345`
            - Buscar por fecha: `/api/paquetes/buscar?fechaRegistro=2025-11-17`
            - Buscar en tránsito: `/api/paquetes/buscar?nombreEstado=EN_TRANSITO`
            - Combinar criterios: `/api/paquetes/buscar?fechaRegistro=2025-11-17&nombreEstado=EN_TRANSITO`
            
            **Estados válidos:**
            - REGISTRADO
            - EN_TRANSITO
            - EN_BODEGA
            - EN_REPARTO
            - ENTREGADO
            - CANCELADO
            - DEVUELTO
            
            **Respuestas:**
            - 200: Lista de paquetes encontrados
            - 204: No se encontraron paquetes
            - 400: Parámetros inválidos
            - 401: No autenticado
            """
    )
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPaquetes(
        @Parameter(description = "Código del paquete (número de guía)", example = "PKG-2025-001234")
        @RequestParam(required = false) String codigoPaquete,
        
        @Parameter(description = "Fecha de registro en formato YYYY-MM-DD", example = "2025-11-17")
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaRegistro,
        
        @Parameter(description = "Estado del paquete", example = "EN_TRANSITO")
        @RequestParam(required = false) String nombreEstado
    ) {
        try {
            // Validar que se proporcione al menos un criterio
            if (codigoPaquete == null && fechaRegistro == null && nombreEstado == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Debe proporcionar al menos un criterio de búsqueda");
                error.put("parametrosDisponibles", List.of("codigoPaquete", "fechaRegistro", "nombreEstado"));
                error.put("ejemplo", "/api/paquetes/buscar?fechaRegistro=2025-11-17");
                return ResponseEntity.badRequest().body(error);
            }
            
            List<Paquete> paquetes = paqueteService.buscarPorCriterios(
                codigoPaquete, 
                fechaRegistro, 
                nombreEstado
            );
            
            if (paquetes.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("mensaje", "No se encontraron paquetes con los criterios especificados");
                response.put("criteriosBusqueda", Map.of(
                    "codigoPaquete", codigoPaquete != null ? codigoPaquete : "No especificado",
                    "fechaRegistro", fechaRegistro != null ? fechaRegistro.toString() : "No especificado",
                    "nombreEstado", nombreEstado != null ? nombreEstado : "No especificado"
                ));
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
            }
            
            // Convertir entidades a DTOs con enlaces HATEOAS
            List<EntityModel<PaqueteResponseDto>> paquetesConEnlaces = paquetes.stream()
                .map(p -> {
                    PaqueteResponseDto dto = PaqueteResponseDto.builder()
                        .id(p.getId())
                        .codigoPaquete(p.getCodigoPaquete())
                        .remitente(p.getRemitente())
                        .destinatario(p.getDestinatario())
                        .destino(p.getDestino())
                        .estadoActual(p.getIdEstadoActual() != null ? p.getIdEstadoActual().getNombreEstado() : null)
                        .build();
                    
                    EntityModel<PaqueteResponseDto> entityModel = EntityModel.of(dto);
                    entityModel.add(linkTo(methodOn(PaqueteController.class)
                        .consultarPorCodigo(dto.getCodigoPaquete())).withSelfRel());
                    return entityModel;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Paquetes encontrados");
            response.put("cantidad", paquetesConEnlaces.size());
            response.put("paquetes", paquetesConEnlaces);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al buscar paquetes");
            error.put("detalle", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(
        summary = "Obtener paquetes en tránsito",
        description = """
            Retorna todos los paquetes que están actualmente en estado EN_TRANSITO.
            Este endpoint es útil para ver qué paquetes están activos y en movimiento.
            
            **Estados considerados "en tránsito":**
            - EN_TRANSITO
            
            **Respuestas:**
            - 200: Lista de paquetes en tránsito
            - 204: No hay paquetes en tránsito actualmente
            - 401: No autenticado
            """
    )
    @GetMapping("/en-transito")
    public ResponseEntity<?> obtenerPaquetesEnTransito() {
        try {
            List<Paquete> paquetes = paqueteService.findPaquetesEnTransito();
            
            if (paquetes.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("mensaje", "No hay paquetes en tránsito en este momento");
                response.put("cantidad", 0);
                response.put("paquetes", List.of());
                return ResponseEntity.ok(response);
            }
            
            // Convertir entidades a DTOs con enlaces HATEOAS
            List<EntityModel<PaqueteResponseDto>> paquetesConEnlaces = paquetes.stream()
                .map(p -> {
                    PaqueteResponseDto dto = PaqueteResponseDto.builder()
                        .id(p.getId())
                        .codigoPaquete(p.getCodigoPaquete())
                        .remitente(p.getRemitente())
                        .destinatario(p.getDestinatario())
                        .destino(p.getDestino())
                        .estadoActual(p.getIdEstadoActual() != null ? p.getIdEstadoActual().getNombreEstado() : null)
                        .build();
                    
                    EntityModel<PaqueteResponseDto> entityModel = EntityModel.of(dto);
                    entityModel.add(linkTo(methodOn(PaqueteController.class)
                        .consultarPorCodigo(dto.getCodigoPaquete())).withSelfRel());
                    entityModel.add(linkTo(methodOn(PaqueteController.class)
                        .consultarEnRuta(dto.getCodigoPaquete())).withRel("en-ruta"));
                    return entityModel;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Paquetes en tránsito encontrados");
            response.put("cantidad", paquetesConEnlaces.size());
            response.put("paquetes", paquetesConEnlaces);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener paquetes en tránsito");
            error.put("detalle", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Actualizar dirección de destino de un paquete",
            description = "Permite modificar la dirección de destino de un paquete existente")
    @PutMapping("/{codigo}/direccion")
    public ResponseEntity<PaqueteResponseDto> actualizarDireccionDestino(
            @PathVariable String codigo,
            @Valid @RequestBody PaqueteDireccionUpdateRequest request) {
        return ResponseEntity.ok(paqueteService.actualizarDireccion(codigo, request));
    }

    @Operation(summary = "Actualizar estado de un paquete",
            description = "Cambia el estado actual del paquete y registra el cambio en el historial")
    @PutMapping("/{codigo}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable String codigo,
            @RequestParam String nuevoEstado) {
        try {
            PaqueteResponseDto paquete = paqueteService.actualizarEstado(codigo, nuevoEstado);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Estado actualizado exitosamente");
            response.put("paquete", paquete);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar el estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Ping",
            description = "Endpoint de prueba para verificar que el servicio está activo")
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}