package com.example.TrabajoMyDAI.data.services;

import com.example.TrabajoMyDAI.data.model.Recordatorio;
import com.example.TrabajoMyDAI.data.repository.RecordatorioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RecordatorioSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(RecordatorioSchedulerService.class);
    private final RecordatorioRepository recordatorioRepository;
    private final NotificationService notificationService;

    // Para evitar notificar múltiples veces el mismo recordatorio
    private final Set<Long> recordatoriosNotificados = new HashSet<>();

    public RecordatorioSchedulerService(RecordatorioRepository recordatorioRepository,
                                       NotificationService notificationService) {
        this.recordatorioRepository = recordatorioRepository;
        this.notificationService = notificationService;
    }

    /**
      * Verifica cada 30 segundos si hay recordatorios que deben notificarse
     * Se ejecuta cada 30 segundos para mayor precisión
     */
    @Scheduled(fixedRate = 30000)
    public void verificarRecordatorios() {
        LocalDateTime ahora = LocalDateTime.now();

        logger.info("=== Verificando recordatorios - Hora actual: {} ===", ahora);

        // Obtener todos los recordatorios
        List<Recordatorio> todosRecordatorios = recordatorioRepository.findAll();

        logger.info("Total de recordatorios en BD: {}", todosRecordatorios.size());

        int recordatoriosProcesados = 0;
        for (Recordatorio recordatorio : todosRecordatorios) {
            if (recordatorio.getFecha() == null) {
                continue;
            }

            LocalDateTime fechaRecordatorio = recordatorio.getFecha();

            // Verificar si la fecha del recordatorio es igual o anterior a ahora (con tolerancia de 1 minuto)
            // y no ha sido notificado antes
            long diferenciaSegundos = java.time.Duration.between(fechaRecordatorio, ahora).getSeconds();

            logger.info("Recordatorio ID {}: Fecha programada: {}, Diferencia en segundos: {}",
                recordatorio.getId_recordatorio(), fechaRecordatorio, diferenciaSegundos);

            // Si el recordatorio debe activarse (la fecha ya pasó y no se notificó, con tolerancia de 2 minutos)
            // Cambiado: ahora comprobamos si está en el rango de -60 a +120 segundos (1 min antes a 2 min después)
            if (!recordatoriosNotificados.contains(recordatorio.getId_recordatorio()) &&
                diferenciaSegundos >= -60 && diferenciaSegundos <= 120) {

                String nombreEvento = recordatorio.getEvento() != null
                    ? recordatorio.getEvento().getNombre()
                    : "Sin evento";

                String mensaje = recordatorio.getMensaje() != null
                    ? recordatorio.getMensaje()
                    : "Recordatorio";

                logger.info("¡ENVIANDO NOTIFICACIÓN! Recordatorio ID: {}, Mensaje: {}, Evento: {}",
                    recordatorio.getId_recordatorio(), mensaje, nombreEvento);

                notificationService.notificarRecordatorioProximo(mensaje, nombreEvento);

                // Marcar como notificado
                recordatoriosNotificados.add(recordatorio.getId_recordatorio());
                recordatoriosProcesados++;

                logger.info("✓ Notificación enviada exitosamente para recordatorio ID: {}",
                    recordatorio.getId_recordatorio());
            }
        }

        logger.info("Recordatorios procesados en este ciclo: {}", recordatoriosProcesados);
        logger.info("Total de recordatorios ya notificados: {}", recordatoriosNotificados.size());

        // Eliminar recordatorios pasados (después de 1 hora)
        eliminarRecordatoriosPasadosInmediato();

        // Limpiar recordatorios antiguos del conjunto de notificados
        limpiarRecordatoriosAntiguos(todosRecordatorios);
    }

    /**
     * Limpia el registro de recordatorios notificados si ya pasó su fecha
     */
    private void limpiarRecordatoriosAntiguos(List<Recordatorio> todosRecordatorios) {
        LocalDateTime hace1Hora = LocalDateTime.now().minusHours(1);

        Set<Long> idsActuales = new HashSet<>();
        for (Recordatorio r : todosRecordatorios) {
            if (r.getFecha() != null && r.getFecha().isAfter(hace1Hora)) {
                idsActuales.add(r.getId_recordatorio());
            }
        }

        // Remover IDs que ya no existen o son muy antiguos
        recordatoriosNotificados.retainAll(idsActuales);
    }

    /**
     * Elimina automáticamente los recordatorios que ya pasaron hace más de 1 hora
     * Se ejecuta cada vez que se verifica (cada minuto)
     */
    private void eliminarRecordatoriosPasadosInmediato() {
        LocalDateTime hace1Hora = LocalDateTime.now().minusHours(1);

        List<Recordatorio> todosRecordatorios = recordatorioRepository.findAll();
        int eliminados = 0;

        for (Recordatorio recordatorio : todosRecordatorios) {
            if (recordatorio.getFecha() != null && recordatorio.getFecha().isBefore(hace1Hora)) {
                logger.info("Eliminando recordatorio pasado ID: {} - Fecha: {}",
                    recordatorio.getId_recordatorio(), recordatorio.getFecha());
                recordatorioRepository.delete(recordatorio);
                recordatoriosNotificados.remove(recordatorio.getId_recordatorio());
                eliminados++;
            }
        }

        if (eliminados > 0) {
            logger.info("Recordatorios eliminados: {}", eliminados);
        }
    }
}


