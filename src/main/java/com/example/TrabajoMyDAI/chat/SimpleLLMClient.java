// java
package com.example.TrabajoMyDAI.chat;

import com.example.TrabajoMyDAI.data.model.Evento;
import com.example.TrabajoMyDAI.data.model.Jugador;
import com.example.TrabajoMyDAI.data.model.Ticket;
import com.example.TrabajoMyDAI.data.model.Usuario;
import com.example.TrabajoMyDAI.data.repository.EventoRepository;
import com.example.TrabajoMyDAI.data.repository.JugadorRepository;
import com.example.TrabajoMyDAI.data.repository.TicketRepository;
import com.example.TrabajoMyDAI.data.services.UsuarioService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class SimpleLLMClient implements LLMClient {

    @Value("${google.ai.api.key}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventoRepository eventoRepository;
    private final JugadorRepository jugadorRepository;
    private final TicketRepository ticketRepository;
    private final UsuarioService usuarioService;

    public SimpleLLMClient(EventoRepository eventoRepository,
                          JugadorRepository jugadorRepository,
                          TicketRepository ticketRepository,
                          UsuarioService usuarioService) {
        this.eventoRepository = eventoRepository;
        this.jugadorRepository = jugadorRepository;
        this.ticketRepository = ticketRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public String generateResponse(String sessionId, String prompt) throws Exception {
        // Construir contexto con información de la base de datos
        String context = buildContext(prompt);

        // Construir prompt enriquecido
        String enrichedPrompt = buildEnrichedPrompt(prompt, context);

        // Llamar a la API de Gemini
        return callGeminiAPI(enrichedPrompt);
    }

    private String buildContext(String userPrompt) {
        StringBuilder context = new StringBuilder();
        String promptLower = userPrompt.toLowerCase();

        // Detectar si pregunta sobre jugadores
        if (promptLower.contains("jugador") || promptLower.contains("plantilla") ||
            promptLower.contains("dorsal") || promptLower.contains("posicion")) {
            context.append("\n### INFORMACIÓN DE JUGADORES ###\n");
            List<Jugador> jugadores = StreamSupport.stream(jugadorRepository.findAll().spliterator(), false)
                    .collect(Collectors.toList());

            for (Jugador j : jugadores) {
                context.append(String.format("- %s: Dorsal %d, %s, %d años, %s\n",
                        j.getNombre(), j.getDorsal(), j.getPosicion(), j.getEdad(), j.getNacionalidad()));
            }
        }

        // Detectar si pregunta sobre eventos
        if (promptLower.contains("evento") || promptLower.contains("partido") ||
            promptLower.contains("próximo") || promptLower.contains("último")) {
            context.append("\n### INFORMACIÓN DE EVENTOS ###\n");
            List<Evento> eventos = StreamSupport.stream(eventoRepository.findAll().spliterator(), false)
                    .sorted(Comparator.comparing(Evento::getFecha).reversed())
                    .limit(5)
                    .collect(Collectors.toList());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (Evento e : eventos) {
                context.append(String.format("- %s: %s en %s (Tipo: %s)\n",
                        e.getNombre(), e.getFecha().format(formatter), e.getLugar(), e.getTipo()));
            }
        }

        // Detectar si pregunta sobre tickets
        if (promptLower.contains("ticket") || promptLower.contains("entrada") ||
            promptLower.contains("comprar") || promptLower.contains("mis entradas")) {
            context.append("\n### INFORMACIÓN DE TICKETS ###\n");
            // Por ahora, información general sobre tickets
            context.append("Para consultar o comprar tickets, visita la sección de Tickets en el menú.\n");

            /* TODO: Descomentar cuando se agregue Spring Security
            try {
                // Intentar obtener el usuario actual
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                    Usuario usuario = usuarioService.buscarUsuarioPorUsername(auth.getName());
                    if (usuario != null) {
                        List<Ticket> tickets = ticketRepository.findByUsuario(usuario);
                        if (tickets.isEmpty()) {
                            context.append("No tienes tickets comprados actualmente.\n");
                        } else {
                            for (Ticket t : tickets) {
                                context.append(String.format("- Ticket #%d: %s, Zona: %s, Asiento: %d, Precio: %.2f€\n",
                                        t.getId_ticket(), t.getEvento().getNombre(),
                                        t.getZona().getNombre(), t.getAsiento(), t.getPrecio()));
                            }
                        }
                    }
                } else {
                    context.append("Usuario no autenticado. Para consultar tickets, debes iniciar sesión.\n");
                }
            } catch (Exception e) {
                context.append("No se pudo obtener información de tickets del usuario.\n");
            }
            */
        }

        return context.toString();
    }

    private String buildEnrichedPrompt(String userPrompt, String context) {
        StringBuilder fullPrompt = new StringBuilder();

        fullPrompt.append("Eres un asistente virtual del FC Barcelona Atlètic. ");
        fullPrompt.append("Tu trabajo es ayudar a los fans con información sobre el equipo, jugadores, eventos y tickets.\n\n");

        if (!context.isEmpty()) {
            fullPrompt.append("INFORMACIÓN DE LA BASE DE DATOS:");
            fullPrompt.append(context);
            fullPrompt.append("\n\n");
        }

        fullPrompt.append("PREGUNTA DEL USUARIO: ");
        fullPrompt.append(userPrompt);
        fullPrompt.append("\n\n");
        fullPrompt.append("Responde de manera amigable, concisa y útil. ");
        fullPrompt.append("Si la información está en la base de datos, úsala. ");
        fullPrompt.append("Si no hay información disponible, sugiérele al usuario que consulte otras secciones de la web.");

        return fullPrompt.toString();
    }

    private String callGeminiAPI(String prompt) throws Exception {
        // Construir el JSON request
        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contents = requestBody.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt);

        // URL de la API de Gemini
        String url = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=%s",
                apiKey
        );

        String requestJson = requestBody.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error en la API de Gemini: " + response.body());
        }

        // Parsear la respuesta
        JsonNode rootNode = objectMapper.readTree(response.body());
        JsonNode candidatesNode = rootNode.path("candidates");

        if (candidatesNode.isArray() && candidatesNode.size() > 0) {
            JsonNode firstCandidate = candidatesNode.get(0);
            JsonNode contentNode = firstCandidate.path("content");
            JsonNode partsNode = contentNode.path("parts");

            if (partsNode.isArray() && partsNode.size() > 0) {
                JsonNode firstPart = partsNode.get(0);
                String text = firstPart.path("text").asText();
                return text;
            }
        }

        return "Lo siento, no pude generar una respuesta en este momento.";
    }
}
