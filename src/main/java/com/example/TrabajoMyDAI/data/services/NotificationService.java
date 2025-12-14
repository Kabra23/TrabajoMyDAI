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
     * Muestra una notificación de Windows usando PowerShell con Toast Notifications modernas
     */
    public void mostrarNotificacion(String titulo, String mensaje) {
        logger.info("=== INICIANDO ENVÍO DE NOTIFICACIÓN ===");
        logger.info("Título: {}", titulo);
        logger.info("Mensaje: {}", mensaje);

        try {
            // Limpiar el texto de caracteres problemáticos
            String tituloLimpio = limpiarTexto(titulo);
            String mensajeLimpio = limpiarTexto(mensaje);

            // Intentar primero con Toast Notifications modernas de Windows 10/11
            String powershellScriptToast = crearScriptToastNotification(tituloLimpio, mensajeLimpio);

            ProcessBuilder processBuilder = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-ExecutionPolicy", "Bypass",
                "-WindowStyle", "Hidden",
                "-Command",
                powershellScriptToast
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
                logger.warn("⚠ Error al enviar notificación toast. Intentando método alternativo...");
                // Si falla, usar el método alternativo con balloon tip
                mostrarNotificacionAlternativa(tituloLimpio, mensajeLimpio);
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
     * Crea un script PowerShell para Toast Notifications modernas de Windows 10/11
     */
    private String crearScriptToastNotification(String titulo, String mensaje) {
        // XML para Toast Notification moderna con icono de Barça
        return "[Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime] > $null;" +
            "[Windows.Data.Xml.Dom.XmlDocument, Windows.Data.Xml.Dom.XmlDocument, ContentType = WindowsRuntime] > $null;" +
            "$APP_ID = '{1AC14E77-02E7-4E5D-B744-2EB1AE5198B7}\\WindowsPowerShell\\v1.0\\powershell.exe';" +
            "$template = @\"" +
            "<toast>" +
            "  <visual>" +
            "    <binding template='ToastGeneric'>" +
            "      <text><![CDATA[" + titulo + "]]></text>" +
            "      <text><![CDATA[" + mensaje + "]]></text>" +
            "      <image placement='appLogoOverride' hint-crop='circle' src='https://upload.wikimedia.org/wikipedia/en/thumb/4/47/FC_Barcelona_(crest).svg/200px-FC_Barcelona_(crest).svg.png'/>" +
            "    </binding>" +
            "  </visual>" +
            "  <audio src='ms-winsoundevent:Notification.Default'/>" +
            "</toast>" +
            "\"@;" +
            "$xml = New-Object Windows.Data.Xml.Dom.XmlDocument;" +
            "$xml.LoadXml($template);" +
            "$toast = New-Object Windows.UI.Notifications.ToastNotification $xml;" +
            "[Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier($APP_ID).Show($toast);";
    }

    /**
     * Método alternativo usando balloon tip si Toast Notifications fallan
     */
    private void mostrarNotificacionAlternativa(String titulo, String mensaje) {
        try {
            String powershellScript =
                "Add-Type -AssemblyName System.Windows.Forms; " +
                "Add-Type -AssemblyName System.Drawing; " +
                "$global:balloon = New-Object System.Windows.Forms.NotifyIcon; " +
                "$balloon.Icon = [System.Drawing.SystemIcons]::Information; " +
                "$balloon.BalloonTipIcon = [System.Windows.Forms.ToolTipIcon]::Info; " +
                "$balloon.BalloonTipText = '" + mensaje + "'; " +
                "$balloon.BalloonTipTitle = '" + titulo + "'; " +
                "$balloon.Visible = $true; " +
                "$balloon.ShowBalloonTip(10000);";

            ProcessBuilder processBuilder = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-ExecutionPolicy", "Bypass",
                "-WindowStyle", "Hidden",
                "-Command",
                powershellScript
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("✓ Notificación alternativa enviada correctamente: {}", titulo);
            } else {
                logger.error("✗ Error al enviar notificación alternativa. Código: {}", exitCode);
            }
        } catch (Exception e) {
            logger.error("✗ Error en notificación alternativa: {}", e.getMessage(), e);
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

