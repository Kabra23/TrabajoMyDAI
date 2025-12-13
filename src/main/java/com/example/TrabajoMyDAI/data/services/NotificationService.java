package com.example.TrabajoMyDAI.data.services;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    /**
     * Muestra una notificación de Windows usando PowerShell
     */
    public void mostrarNotificacion(String titulo, String mensaje) {
        logger.info("=== INICIANDO ENVÍO DE NOTIFICACIÓN ===");
        logger.info("Título: {}", titulo);
        logger.info("Mensaje: {}", mensaje);

        try {
            // Limpiar el texto de caracteres problemáticos
            String tituloLimpio = limpiarTexto(titulo);
            String mensajeLimpio = limpiarTexto(mensaje);

            // Usar el enfoque más simple con Add-Type
            String powershellScript =
                "Add-Type -AssemblyName System.Windows.Forms; " +
                "Add-Type -AssemblyName System.Drawing; " +
                "$global:balloon = New-Object System.Windows.Forms.NotifyIcon; " +
                "$balloon.Icon = [System.Drawing.SystemIcons]::Information; " +
                "$balloon.BalloonTipIcon = [System.Windows.Forms.ToolTipIcon]::Info; " +
                "$balloon.BalloonTipText = '" + mensajeLimpio + "'; " +
                "$balloon.BalloonTipTitle = '" + tituloLimpio + "'; " +
                "$balloon.Visible = $true; " +
                "$balloon.ShowBalloonTip(10000);";

            logger.debug("Script PowerShell preparado");

            ProcessBuilder processBuilder = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-ExecutionPolicy", "Bypass",
                "-Command",
                powershellScript
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Leer la salida del proceso
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("✓ Notificación enviada correctamente: {}", titulo);
            } else {
                logger.error("✗ Error al enviar notificación. Código de salida: {}", exitCode);
                if (output.length() > 0) {
                    logger.error("Salida del proceso: {}", output.toString());
                }
            }

        } catch (IOException e) {
            logger.error("✗ Error de IO al mostrar notificación: {}", e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error("✗ Proceso interrumpido al mostrar notificación: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("✗ Error inesperado al mostrar notificación: {}", e.getMessage(), e);
        }
    }

    /**
     * Limpia el texto de caracteres problemáticos para PowerShell
     */
    private String limpiarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        // Escapar comillas simples duplicándolas
        return texto.replace("'", "''");
    }

    /**
     * Muestra una notificación simple cuando se crea un recordatorio
     */
    public void notificarRecordatorioCreado(String mensajeRecordatorio, String fechaHora) {
        logger.info("Notificando creación de recordatorio: {} para {}", mensajeRecordatorio, fechaHora);
        String titulo = "Recordatorio Creado";
        String mensaje = String.format("Se ha creado un recordatorio para el %s - %s",
            fechaHora, mensajeRecordatorio);
        mostrarNotificacion(titulo, mensaje);
    }

    /**
     * Muestra una notificación cuando se aproxima la fecha del recordatorio
     */
    public void notificarRecordatorioProximo(String mensajeRecordatorio, String nombreEvento) {
        logger.info("Notificando recordatorio próximo: {} - Evento: {}", mensajeRecordatorio, nombreEvento);
        String titulo = "RECORDATORIO";
        String mensaje = String.format("%s - Evento: %s", mensajeRecordatorio, nombreEvento);
        mostrarNotificacion(titulo, mensaje);
    }
}

