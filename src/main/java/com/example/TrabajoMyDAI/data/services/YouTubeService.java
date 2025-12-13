package com.example.TrabajoMyDAI.data.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class YouTubeService {

    @Value("${youtube.api.key:}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Busca un video de YouTube y devuelve el primer resultado
     * Si no hay API key configurada, devuelve una URL de búsqueda
     */
    public String buscarVideoJugador(String nombreJugador) {
        // Si hay API key, usar la API de YouTube
        if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("")) {
            return buscarConAPI(nombreJugador);
        }

        // Si no hay API key, usar búsqueda directa
        return buscarSinAPI(nombreJugador);
    }

    private String buscarConAPI(String nombreJugador) {
        try {
            String query = "Highlights " + nombreJugador + " FC Barcelona";
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

            String url = String.format(
                "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=1&q=%s&key=%s",
                encodedQuery, apiKey
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(response.body());
                JsonNode items = rootNode.path("items");

                if (items.isArray() && items.size() > 0) {
                    String videoId = items.get(0).path("id").path("videoId").asText();
                    if (videoId != null && !videoId.isEmpty()) {
                        return "https://www.youtube.com/embed/" + videoId;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error buscando video con API de YouTube: " + e.getMessage());
        }

        // Si falla la API, usar búsqueda directa
        return buscarSinAPI(nombreJugador);
    }

    private String buscarSinAPI(String nombreJugador) {
        try {
            // Optimizar la búsqueda para Barça Atlètic / FC Barcelona B
            String searchQuery = "Highlights " + nombreJugador + " FC Barcelona";
            String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);

            // Usar el formato de YouTube que carga el primer resultado de búsqueda
            // Esta URL mostrará los resultados de búsqueda en el iframe
            return "https://www.youtube.com/embed?listType=search&list=" + encodedQuery;

        } catch (Exception e) {
            System.err.println("Error generando URL de búsqueda de YouTube: " + e.getMessage());
            return null;
        }
    }

    /**
     * Método alternativo que genera un enlace directo a YouTube (no embebido)
     * para que el usuario pueda abrir en nueva pestaña
     */
    public String generarEnlaceYouTube(String nombreJugador) {
        try {
            String searchQuery = "Highlights " + nombreJugador + " FC Barcelona";
            String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);

            return "https://www.youtube.com/results?search_query=" + encodedQuery;

        } catch (Exception e) {
            return null;
        }
    }
}

