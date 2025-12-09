package com.example.TrabajoMyDAI.data.services;

import com.example.TrabajoMyDAI.data.model.Jugador;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Service
public class GeminiService {

    @Value("${google.ai.api.key}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateImageWithPlayer(MultipartFile userImage, Jugador jugador) throws IOException, InterruptedException {
        System.out.println("=== INICIANDO GENERACION CON GEMINI ===");
        System.out.println("Jugador: " + jugador.getNombre());
        System.out.println("Tamaño imagen: " + userImage.getSize() + " bytes");
        System.out.println("Tipo MIME: " + userImage.getContentType());

        // Verificar y ajustar el tipo MIME si es necesario
        String mimeType = userImage.getContentType();
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = "image/jpeg"; // Default
        }

        String base64Image = Base64.getEncoder().encodeToString(userImage.getBytes());
        System.out.println("Imagen codificada en Base64: " + base64Image.length() + " caracteres");

        // Limpiar los datos del jugador para evitar caracteres problemáticos
        String nombreLimpio = limpiarTexto(jugador.getNombre());
        String posicionLimpia = limpiarTexto(jugador.getPosicion());

        String prompt = String.format(
                "Analiza esta foto y describe cómo se vería una escena donde esta persona aparece " +
                        "junto al jugador de fútbol %s (dorsal número %d, %s) del FC Barcelona Atlètic " +
                        "en el estadio Johan Cruyff. Describe la composición, el ambiente, las expresiones " +
                        "y cómo se verían juntos posando amistosamente con el uniforme del Barça de fondo.",
                nombreLimpio,
                jugador.getDorsal(),
                posicionLimpia
        );

        // Construir el JSON request manualmente para mayor control
        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contents = requestBody.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");

        // Primero el texto
        parts.addObject().put("text", prompt);

        // Luego la imagen
        ObjectNode imagePart = parts.addObject();
        ObjectNode inlineData = imagePart.putObject("inline_data");
        inlineData.put("mime_type", mimeType);
        inlineData.put("data", base64Image);

        // Usar gemini-2.5-flash - modelo correcto para procesar imágenes
        String url = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=%s",
                apiKey
        );

        System.out.println("URL API: " + url.replace(apiKey, "***KEY***"));
        String requestJson = requestBody.toString();
        System.out.println("Request Body size: " + requestJson.length() + " bytes");

        // Mostrar una muestra del request (sin la imagen completa)
        if (requestJson.length() > 500) {
            System.out.println("Request sample: " + requestJson.substring(0, 500) + "...");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        System.out.println("Enviando petición a Gemini API...");

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Respuesta recibida:");
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
            System.out.println("=== FIN GENERACION GEMINI ===");

            // Si hay error, lanzar excepción con el mensaje limpio
            if (response.statusCode() != 200) {
                String errorMsg = limpiarMensajeError(response.body());
                throw new IOException("Error de Gemini API (Status " + response.statusCode() + "): " + errorMsg);
            }

            return response.body();

        } catch (IOException e) {
            System.err.println("ERROR EN GEMINI API: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (InterruptedException e) {
            System.err.println("ERROR EN GEMINI API (Interrupción): " + e.getMessage());
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new IOException("Petición interrumpida", e);
        }
    }

    private String limpiarTexto(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        // Remover caracteres de control pero mantener espacios normales
        return texto
                .replaceAll("[\\r\\n\\t\\x00-\\x1F\\x7F]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String limpiarMensajeError(String mensaje) {
        if (mensaje == null || mensaje.isEmpty()) {
            return "Error desconocido";
        }
        // Limitar longitud y remover caracteres de control
        String limpio = mensaje
                .replaceAll("[\\r\\n\\t]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        // Limitar longitud del mensaje
        if (limpio.length() > 200) {
            limpio = limpio.substring(0, 200) + "...";
        }

        return limpio;
    }
}

