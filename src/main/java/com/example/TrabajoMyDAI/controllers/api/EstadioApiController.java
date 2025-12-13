package com.example.TrabajoMyDAI.controllers.api;

import com.example.TrabajoMyDAI.data.model.Zona;
import com.example.TrabajoMyDAI.data.services.ZonaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/eventos")
public class EstadioApiController {

    private final ZonaService zonaService;

    public EstadioApiController(ZonaService zonaService) {
        this.zonaService = zonaService;
    }

    /**
     * Obtener zonas con información de disponibilidad y precios
     */
    @GetMapping("/{id}/estadio/zonas")
    public ResponseEntity<Map<String, Object>> obtenerZonas(@PathVariable("id") Long eventoId) {
        try {
            List<Zona> zonas = zonaService.obtenerZonasPorEvento(eventoId);
            
            List<Map<String, Object>> zonasData = zonas.stream().map(zona -> {
                Map<String, Object> zonaMap = new HashMap<>();
                zonaMap.put("id", zona.getId());
                zonaMap.put("nombre", zona.getNombre());
                zonaMap.put("capacidadTotal", zona.getCapacidadTotal());
                zonaMap.put("entradasVendidas", zona.getEntradasVendidas());
                zonaMap.put("precio", zona.getPrecio() != null ? zona.getPrecio() : 0.0);
                zonaMap.put("disponibles", zona.getEntradasDisponibles());
                return zonaMap;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("zonas", zonasData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al obtener zonas: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtener asientos ocupados por zona
     */
    @GetMapping("/{id}/estadio/asientos-ocupados")
    public ResponseEntity<Map<String, List<Long>>> obtenerAsientosOcupados(@PathVariable("id") Long eventoId) {
        try {
            Map<String, List<Long>> asientosOcupados = zonaService.obtenerAsientosOcupadosPorEvento(eventoId);
            return ResponseEntity.ok(asientosOcupados);
        } catch (Exception e) {
            Map<String, List<Long>> errorMap = new HashMap<>();
            // Return empty lists for each zone in case of error
            errorMap.put("error", new java.util.ArrayList<>());
            return ResponseEntity.internalServerError().body(errorMap);
        }
    }

    /**
     * Reservar asiento (endpoint de ejemplo - se puede extender según necesidades)
     */
    @PostMapping("/{id}/estadio/reservar")
    public ResponseEntity<Map<String, Object>> reservarAsiento(
            @PathVariable("id") Long eventoId,
            @RequestBody Map<String, Object> reserva) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Reserva temporal creada");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
