package com. example.TrabajoMyDAI.data.services;

import com.example.TrabajoMyDAI.data.exceptions.ValidationException;
import com. example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.repository.EventoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream. Collectors;
import java.util.stream.StreamSupport;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;

    public EventoService(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    /**
     * Crear un nuevo evento con validaciones
     */
    public Evento crearEvento(Evento evento) {
        // Validaciones
        if (evento.getNombre() == null || evento.getNombre().trim().isEmpty()) {
            throw new ValidationException("El nombre del evento es obligatorio.");
        }

        if (evento.getFecha() == null) {
            throw new ValidationException("La fecha del evento es obligatoria.");
        }

        if (evento.getFecha().isBefore(LocalDateTime.now())) {
            throw new ValidationException("La fecha del evento no puede ser anterior a la fecha actual.");
        }

        if (evento.getLugar() == null || evento.getLugar().trim().isEmpty()) {
            throw new ValidationException("El lugar del evento es obligatorio.");
        }

        if (evento.getCapacidad() != null && evento.getCapacidad() <= 0) {
            throw new ValidationException("La capacidad debe ser mayor que 0.");
        }

        return eventoRepository.save(evento);
    }

    /**
     * Obtener todos los eventos
     */
    public List<Evento> obtenerTodosLosEventos() {
        Iterable<Evento> iterable = eventoRepository.findAll();
        return StreamSupport. stream(iterable.spliterator(), false)
                .collect(Collectors.toList());
    }

    /**
     * Obtener evento por ID
     */
    public Optional<Evento> obtenerEventoPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de evento inválido: " + id);
        }
        return eventoRepository.findById(id);
    }

    public Evento actualizarEvento(Long id, Evento eventoActualizado) {
        if (eventoActualizado == null) {
            throw new ValidationException("Los datos del evento no pueden ser nulos.");
        }

        Optional<Evento> eventoExistente = eventoRepository.findById(id);

        if (eventoExistente.isEmpty()) {
            throw new ValidationException("Evento con ID " + id + " no encontrado.");
        }

        Evento evento = eventoExistente.get();

        // Validaciones y actualizaciones con verificación de null
        if (eventoActualizado.getNombre() != null && !eventoActualizado.getNombre().trim().isEmpty()) {
            evento.setNombre(eventoActualizado.getNombre());
        }

        if (eventoActualizado.getFecha() != null) {
            if (eventoActualizado.getFecha().isBefore(LocalDateTime.now())) {
                throw new ValidationException("La fecha del evento no puede ser anterior a la fecha actual.");
            }
            evento.setFecha(eventoActualizado.getFecha());
        }

        if (eventoActualizado.getLugar() != null && !eventoActualizado.getLugar().trim().isEmpty()) {
            evento.setLugar(eventoActualizado.getLugar());
        }

        if (eventoActualizado.getDescripcion() != null) {
            evento.setDescripcion(eventoActualizado.getDescripcion());
        }

        // ✅ Protección adicional para tipo
        if (eventoActualizado.getTipo() != null && !eventoActualizado.getTipo().trim().isEmpty()) {
            evento.setTipo(eventoActualizado.getTipo());
        }


        if (eventoActualizado.getCapacidad() != null && eventoActualizado.getCapacidad() > 0) {
            evento.setCapacidad(eventoActualizado.getCapacidad());
        }

        return eventoRepository.save(evento);
    }


    /**
     * Eliminar un evento por ID
     */
    public void eliminarEvento(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de evento inválido: " + id);
        }

        if (!eventoRepository.existsById(id)) {
            throw new ValidationException("Evento con ID " + id + " no encontrado.");
        }

        eventoRepository.deleteById(id);
    }

    /**
     * Obtener eventos futuros (a partir de la fecha actual)
     */
    public List<Evento> obtenerEventosFuturos() {
        return obtenerTodosLosEventos().stream()
                .filter(evento -> evento.getFecha() != null && evento.getFecha().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
    }

    /**
     * Obtener eventos pasados
     */
    public List<Evento> obtenerEventosPasados() {
        return obtenerTodosLosEventos(). stream()
                .filter(evento -> evento.getFecha() != null && evento.getFecha().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
    }

    /**
     * Verificar si un evento tiene capacidad disponible
     */
    public boolean tieneCapacidadDisponible(Long eventoId) {
        Optional<Evento> eventoOpt = eventoRepository.findById(eventoId);

        if (eventoOpt. isEmpty()) {
            return false;
        }

        Evento evento = eventoOpt. get();

        if (evento.getCapacidad() == null) {
            return true; // Sin límite de capacidad
        }

        // Contar usuarios inscritos
        int inscripciones = evento.getUsuarios() != null ? evento.getUsuarios().size() : 0;
        return inscripciones < evento.getCapacidad();
    }

    /**
     * Obtener el número de plazas disponibles
     */
    public Integer obtenerPlazasDisponibles(Long eventoId) {
        Optional<Evento> eventoOpt = eventoRepository.findById(eventoId);

        if (eventoOpt.isEmpty()) {
            throw new ValidationException("Evento no encontrado.");
        }

        Evento evento = eventoOpt.get();

        if (evento.getCapacidad() == null) {
            return null; // Capacidad ilimitada
        }

        int inscripciones = evento.getUsuarios() != null ? evento. getUsuarios().size() : 0;
        return evento.getCapacidad() - inscripciones;
    }
}