package com.example.TrabajoMyDAI.data.services;

import com.example.TrabajoMyDAI.data.exceptions.ValidationException;
import com.example.TrabajoMyDAI.data.model.Recordatorio;
import com.example.TrabajoMyDAI.data.model.Ticket;
import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.repository.RecordatorioRepository;
import com.example.TrabajoMyDAI.data.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RecordatorioService {

    private final RecordatorioRepository recordatorioRepository;
    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;

    public RecordatorioService(RecordatorioRepository recordatorioRepository,
                               TicketRepository ticketRepository,
                               NotificationService notificationService) {
        this.recordatorioRepository = recordatorioRepository;
        this.ticketRepository = ticketRepository;
        this.notificationService = notificationService;
    }

    public Recordatorio crearRecordatorio(Recordatorio recordatorio, Long ticketId, Usuario usuario) {
        if (ticketId == null) {
            throw new ValidationException("Se debe indicar el ticket asociado.");
        }

        Optional<Ticket> optionalTicket = ticketRepository.findById(ticketId);
        if (optionalTicket.isEmpty()) {
            throw new ValidationException("Ticket no encontrado.");
        }
        Ticket ticket = optionalTicket.get();

        if (ticket.getUsuario() == null || !ticket.getUsuario().getDni().equals(usuario.getDni())) {
            throw new ValidationException("El ticket no pertenece al usuario.");
        }

        if (recordatorio.getFecha() == null) {
            throw new ValidationException("Fecha del recordatorio requerida.");
        }

        if (ticket.getEvento() == null || ticket.getEvento().getFecha() == null) {
            throw new ValidationException("El ticket no tiene evento o fecha asociada.");
        }

        LocalDateTime fechaRecordatorio = recordatorio.getFecha();
        LocalDateTime fechaEvento = ticket.getEvento().getFecha();
        LocalDateTime ahora = LocalDateTime.now();

        if (fechaRecordatorio.isBefore(ahora)) {
            throw new ValidationException("La fecha del recordatorio no puede ser anterior a ahora.");
        }

        if (fechaRecordatorio.isAfter(fechaEvento)) {
            throw new ValidationException("La fecha del recordatorio no puede ser posterior a la fecha del evento del ticket.");
        }

        recordatorio.setUsuario(usuario);
        recordatorio.setEvento(ticket.getEvento());

        Recordatorio recordatorioGuardado = recordatorioRepository.save(recordatorio);

        // Mostrar notificación de Windows cuando se crea el recordatorio
        try {
            String fechaFormateada = recordatorio.getFecha()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            notificationService.notificarRecordatorioCreado(
                recordatorio.getMensaje() != null ? recordatorio.getMensaje() : "Recordatorio creado",
                fechaFormateada
            );
        } catch (Exception e) {
            // Si falla la notificación, no afecta al guardado del recordatorio
            System.err.println("Error al mostrar notificación: " + e.getMessage());
        }

        return recordatorioGuardado;
    }

    public Recordatorio editarRecordatorio(Long recordatorioId, Recordatorio recordatorioActualizado, Usuario usuario) {
        Optional<Recordatorio> recordatorioOpt = recordatorioRepository.findById(recordatorioId);
        if (recordatorioOpt.isEmpty()) {
            throw new ValidationException("Recordatorio no encontrado.");
        }

        Recordatorio recordatorioExistente = recordatorioOpt.get();

        // Verificar que el recordatorio pertenece al usuario
        if (!recordatorioExistente.getUsuario().getDni().equals(usuario.getDni())) {
            throw new ValidationException("No tienes permiso para editar este recordatorio.");
        }

        // Validar fecha
        if (recordatorioActualizado.getFecha() == null) {
            throw new ValidationException("Fecha del recordatorio requerida.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (recordatorioActualizado.getFecha().isBefore(ahora)) {
            throw new ValidationException("La fecha del recordatorio no puede ser anterior a ahora.");
        }

        // Validar que la fecha del recordatorio no sea posterior a la fecha del evento
        if (recordatorioExistente.getEvento() != null && recordatorioExistente.getEvento().getFecha() != null) {
            if (recordatorioActualizado.getFecha().isAfter(recordatorioExistente.getEvento().getFecha())) {
                throw new ValidationException("La fecha del recordatorio no puede ser posterior a la fecha del evento.");
            }
        }

        // Actualizar campos
        recordatorioExistente.setFecha(recordatorioActualizado.getFecha());
        if (recordatorioActualizado.getMensaje() != null) {
            recordatorioExistente.setMensaje(recordatorioActualizado.getMensaje());
        }

        return recordatorioRepository.save(recordatorioExistente);
    }
}
