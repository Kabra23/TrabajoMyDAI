package com.example.TrabajoMyDAI.data.services;

import com.example.TrabajoMyDAI.data.exceptions.ValidationException;
import com.example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.model.Zona;
import com.example.TrabajoMyDAI.data.repository.EventoRepository;
import com.example.TrabajoMyDAI.data.repository.ZonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ZonaService {

    private final ZonaRepository zonaRepository;
    private final EventoRepository eventoRepository;

    public ZonaService(ZonaRepository zonaRepository, EventoRepository eventoRepository) {
        this.zonaRepository = zonaRepository;
        this.eventoRepository = eventoRepository;
    }

    /**
     * Crear zonas predeterminadas para un evento
     */
    @Transactional
    public void crearZonasParaEvento(Evento evento) {
        if (evento == null || evento.getId() == null) {
            throw new ValidationException("El evento debe estar guardado antes de crear zonas.");
        }

        // Verificar si ya existen zonas para este evento
        List<Zona> zonasExistentes = zonaRepository.findByEvento(evento);
        if (!zonasExistentes.isEmpty()) {
            return; // Ya existen zonas, no crear duplicados
        }

        // Crear zonas con capacidades predeterminadas
        Zona tribuna = new Zona("Tribuna", evento, 1000, 80.0);
        Zona gradaLateral = new Zona("Grada Lateral", evento, 1500, 60.0);
        Zona golNord = new Zona("Gol Nord", evento, 800, 45.0);
        Zona golSud = new Zona("Gol Sud", evento, 800, 45.0);

        zonaRepository.save(tribuna);
        zonaRepository.save(gradaLateral);
        zonaRepository.save(golNord);
        zonaRepository.save(golSud);
    }

    /**
     * Crear o actualizar una zona específica
     */
    @Transactional
    public Zona guardarZona(Zona zona) {
        if (zona.getNombre() == null || zona.getNombre().trim().isEmpty()) {
            throw new ValidationException("El nombre de la zona es obligatorio.");
        }
        if (zona.getEvento() == null) {
            throw new ValidationException("La zona debe estar asociada a un evento.");
        }
        if (zona.getCapacidadTotal() != null && zona.getCapacidadTotal() <= 0) {
            throw new ValidationException("La capacidad debe ser mayor que 0.");
        }
        return zonaRepository.save(zona);
    }

    /**
     * Obtener todas las zonas de un evento
     */
    public List<Zona> obtenerZonasPorEvento(Long eventoId) {
        return zonaRepository.findByEventoIdWithTickets(eventoId);
    }

    /**
     * Obtener una zona específica de un evento por nombre
     */
    public Optional<Zona> obtenerZonaPorEventoYNombre(Long eventoId, String nombreZona) {
        Optional<Evento> eventoOpt = eventoRepository.findById(eventoId);
        if (eventoOpt.isEmpty()) {
            throw new ValidationException("Evento no encontrado.");
        }
        return zonaRepository.findByEventoAndNombre(eventoOpt.get(), nombreZona);
    }

    /**
     * Verificar si una zona tiene disponibilidad
     */
    public boolean tieneDisponibilidad(Long zonaId) {
        Optional<Zona> zonaOpt = zonaRepository.findById(zonaId);
        if (zonaOpt.isEmpty()) {
            return false;
        }
        return zonaOpt.get().hayDisponibilidad();
    }

    /**
     * Incrementar el contador de entradas vendidas de una zona
     */
    @Transactional
    public void incrementarEntradasVendidas(Long zonaId) {
        Zona zona = zonaRepository.findById(zonaId)
                .orElseThrow(() -> new ValidationException("Zona no encontrada."));

        if (!zona.hayDisponibilidad()) {
            throw new ValidationException("No hay entradas disponibles en la zona " + zona.getNombre());
        }

        zona.incrementarVendidas();
        zonaRepository.save(zona);
    }

    /**
     * Decrementar el contador de entradas vendidas de una zona (para cancelaciones)
     */
    @Transactional
    public void decrementarEntradasVendidas(Long zonaId) {
        Zona zona = zonaRepository.findById(zonaId)
                .orElseThrow(() -> new ValidationException("Zona no encontrada."));

        zona.decrementarVendidas();
        zonaRepository.save(zona);
    }

    /**
     * Obtener el número total de entradas disponibles para un evento (sumando todas las zonas)
     */
    public Integer obtenerEntradasDisponiblesTotales(Long eventoId) {
        List<Zona> zonas = zonaRepository.findByEventoIdWithTickets(eventoId);

        if (zonas.isEmpty()) {
            return null; // Sin zonas configuradas
        }

        int total = 0;
        for (Zona zona : zonas) {
            Integer disponibles = zona.getEntradasDisponibles();
            if (disponibles == null) {
                return null; // Al menos una zona tiene capacidad ilimitada
            }
            total += disponibles;
        }

        return total;
    }

    /**
     * Obtener el número total de entradas vendidas para un evento
     */
    public Integer obtenerEntradasVendidasTotales(Long eventoId) {
        List<Zona> zonas = zonaRepository.findByEventoIdWithTickets(eventoId);

        int total = 0;
        for (Zona zona : zonas) {
            total += zona.getEntradasVendidas() != null ? zona.getEntradasVendidas() : 0;
        }

        return total;
    }
    /**
     * Obtener asientos ocupados por zona para un evento
     * Devuelve un mapa con el nombre de la zona como clave y la lista de números de asiento como valor
     */
    public java.util.Map<String, java.util.List<Long>> obtenerAsientosOcupadosPorEvento(Long eventoId) {
        List<Zona> zonas = zonaRepository.findByEventoIdWithTickets(eventoId);
        java.util.Map<String, java.util.List<Long>> asientosOcupados = new java.util.HashMap<>();

        for (Zona zona : zonas) {
            java.util.List<Long> asientos = zona.getTickets().stream()
                    .map(com.example.TrabajoMyDAI.data.model.Ticket::getAsiento)
                    .filter(asiento -> asiento != null)
                    .collect(java.util.stream.Collectors.toList());
            asientosOcupados.put(zona.getNombre(), asientos);
        }

        return asientosOcupados;
    }
}

