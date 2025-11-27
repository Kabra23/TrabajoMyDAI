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

    public RecordatorioService(RecordatorioRepository recordatorioRepository,
                               TicketRepository ticketRepository) {
        this.recordatorioRepository = recordatorioRepository;
        this.ticketRepository = ticketRepository;
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

        return recordatorioRepository.save(recordatorio);
    }
}
