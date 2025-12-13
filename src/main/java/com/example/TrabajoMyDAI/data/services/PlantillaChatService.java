package com.example.TrabajoMyDAI.data.services;

import com.example.TrabajoMyDAI.chat.ChatMessage;
import com.example.TrabajoMyDAI.chat.ChatMessageRepository;
import com.example.TrabajoMyDAI.data.model.Jugador;
import com.example.TrabajoMyDAI.data.repository.JugadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class PlantillaChatService {

    private final ChatMessageRepository chatRepo;
    private final JugadorRepository jugadorRepo;
    private final YouTubeService youTubeService;

    public PlantillaChatService(ChatMessageRepository chatRepo, JugadorRepository jugadorRepo, YouTubeService youTubeService) {
        this.chatRepo = chatRepo;
        this.jugadorRepo = jugadorRepo;
        this.youTubeService = youTubeService;
    }

    @Transactional
    public Map<String, Object> handlePlayerQuestion(String sessionId, String userMessage) {
        // Guardar mensaje del usuario
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setSender("USER");
        userMsg.setContent(userMessage);
        chatRepo.save(userMsg);

        // Procesar la pregunta
        Map<String, Object> response = processQuestion(userMessage);

        // Guardar respuesta del bot
        ChatMessage botMsg = new ChatMessage();
        botMsg.setSessionId(sessionId);
        botMsg.setSender("BOT");
        botMsg.setContent((String) response.get("reply"));
        chatRepo.save(botMsg);

        return response;
    }

    private Map<String, Object> processQuestion(String question) {
        Map<String, Object> result = new HashMap<>();
        String lowerQuestion = question.toLowerCase().trim();

        // Obtener todos los jugadores
        List<Jugador> todosJugadores = StreamSupport
            .stream(jugadorRepo.findAll().spliterator(), false)
            .collect(Collectors.toList());

        // Buscar si menciona un jugador específico
        Jugador jugadorEncontrado = null;
        for (Jugador j : todosJugadores) {
            String nombreLower = j.getNombre().toLowerCase();
            if (lowerQuestion.contains(nombreLower)) {
                jugadorEncontrado = j;
                break;
            }
        }

        if (jugadorEncontrado != null) {
            // Respuesta sobre un jugador específico
            result.put("reply", construirRespuestaJugador(jugadorEncontrado));
            result.put("videoUrl", getYouTubeEmbedUrl(jugadorEncontrado));
        } else if (lowerQuestion.contains("portero") || lowerQuestion.contains("porteros")) {
            result.put("reply", construirRespuestaPosicion("Portero", todosJugadores));
            result.put("videoUrl", null);
        } else if (lowerQuestion.contains("defensa") || lowerQuestion.contains("defensas")) {
            result.put("reply", construirRespuestaPosicion("Defensa", todosJugadores));
            result.put("videoUrl", null);
        } else if (lowerQuestion.contains("centrocampista") || lowerQuestion.contains("mediocampista")) {
            result.put("reply", construirRespuestaPosicion("Centrocampista", todosJugadores));
            result.put("videoUrl", null);
        } else if (lowerQuestion.contains("delantero") || lowerQuestion.contains("delanteros") || lowerQuestion.contains("atacante")) {
            result.put("reply", construirRespuestaPosicion("Delantero", todosJugadores));
            result.put("videoUrl", null);
        } else if (lowerQuestion.contains("plantilla") || lowerQuestion.contains("todos") || lowerQuestion.contains("jugadores")) {
            result.put("reply", construirRespuestaPlantilla(todosJugadores));
            result.put("videoUrl", null);
        } else if (lowerQuestion.contains("edad") || lowerQuestion.contains("joven") || lowerQuestion.contains("mayor")) {
            result.put("reply", construirRespuestaEdad(todosJugadores));
            result.put("videoUrl", null);
        } else {
            // Respuesta por defecto con sugerencias
            result.put("reply", construirRespuestaAyuda(todosJugadores));
            result.put("videoUrl", null);
        }

        return result;
    }

    private String construirRespuestaJugador(Jugador jugador) {
        StringBuilder sb = new StringBuilder();
        sb.append("⚽ <strong>").append(jugador.getNombre()).append("</strong>\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━\n\n");

        sb.append("📋 <strong>INFORMACIÓN GENERAL</strong>\n");
        sb.append("👕 Dorsal: <strong>#").append(jugador.getDorsal()).append("</strong>\n");
        sb.append("🎯 Posición: <strong>").append(jugador.getPosicion()).append("</strong>\n");
        sb.append("🎂 Edad: ").append(jugador.getEdad()).append(" años\n");
        sb.append("🌍 Nacionalidad: ").append(jugador.getNacionalidad()).append("\n\n");

        sb.append("📊 <strong>DETALLES DE LA POSICIÓN</strong>\n");
        sb.append(getDetallesPorPosicion(jugador.getPosicion())).append("\n\n");

        sb.append("🏆 <strong>EQUIPO</strong>\n");
        sb.append("Club: FC Barcelona (Barça Atlètic)\n");
        sb.append("Categoría: Primera Federación RFEF\n\n");

        sb.append("🎬 <strong>VIDEO HIGHLIGHTS</strong>\n");
        sb.append("A continuación puedes ver un video con los mejores momentos de ").append(jugador.getNombre()).append(":");

        return sb.toString();
    }

    private String getDetallesPorPosicion(String posicion) {
        return switch (posicion.toLowerCase()) {
            case "portero" -> "• Guardameta del equipo\n• Responsable de proteger la portería\n• Primera línea de construcción del juego";
            case "defensa" -> "• Defensor del equipo\n• Encargado de proteger la zona defensiva\n• Apoyo en la salida de balón";
            case "centrocampista" -> "• Motor del equipo\n• Enlace entre defensa y ataque\n• Control del ritmo del juego";
            case "delantero" -> "• Atacante del equipo\n• Responsable de finalizar jugadas\n• Referencia ofensiva";
            default -> "• Jugador polivalente del equipo";
        };
    }

    private String construirRespuestaPosicion(String posicion, List<Jugador> todosJugadores) {
        List<Jugador> jugadoresPosicion = todosJugadores.stream()
            .filter(j -> j.getPosicion().equalsIgnoreCase(posicion))
            .collect(Collectors.toList());

        if (jugadoresPosicion.isEmpty()) {
            return "No encontré jugadores en la posición de " + posicion + ".";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("⚽ ").append(posicion).append("s en la plantilla (").append(jugadoresPosicion.size()).append("):\n\n");

        for (Jugador j : jugadoresPosicion) {
            sb.append("• ").append(j.getNombre())
              .append(" (#").append(j.getDorsal()).append(") - ")
              .append(j.getEdad()).append(" años\n");
        }

        sb.append("\n💡 Pregúntame sobre cualquiera de ellos para ver su video de highlights.");
        return sb.toString();
    }

    private String construirRespuestaPlantilla(List<Jugador> jugadores) {
        Map<String, Long> countPorPosicion = jugadores.stream()
            .collect(Collectors.groupingBy(Jugador::getPosicion, Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append("🏆 Plantilla del Barça Atlètic:\n\n");
        sb.append("📊 Total de jugadores: ").append(jugadores.size()).append("\n\n");
        sb.append("Por posición:\n");
        countPorPosicion.forEach((pos, count) ->
            sb.append("• ").append(pos).append(": ").append(count).append("\n")
        );

        sb.append("\n💡 Pregúntame por una posición específica o por el nombre de un jugador.");
        return sb.toString();
    }

    private String construirRespuestaEdad(List<Jugador> jugadores) {
        if (jugadores.isEmpty()) {
            return "No hay información de jugadores disponible.";
        }

        Jugador masJoven = jugadores.stream()
            .min(Comparator.comparingInt(Jugador::getEdad))
            .orElse(null);

        Jugador mayor = jugadores.stream()
            .max(Comparator.comparingInt(Jugador::getEdad))
            .orElse(null);

        double edadMedia = jugadores.stream()
            .mapToInt(Jugador::getEdad)
            .average()
            .orElse(0.0);

        StringBuilder sb = new StringBuilder();
        sb.append("📊 Información de edades:\n\n");
        sb.append("📉 Edad media: ").append(String.format("%.1f", edadMedia)).append(" años\n");

        if (masJoven != null) {
            sb.append("👶 Más joven: ").append(masJoven.getNombre())
              .append(" (").append(masJoven.getEdad()).append(" años)\n");
        }

        if (mayor != null) {
            sb.append("👴 Mayor: ").append(mayor.getNombre())
              .append(" (").append(mayor.getEdad()).append(" años)\n");
        }

        return sb.toString();
    }

    private String construirRespuestaAyuda(List<Jugador> jugadores) {
        StringBuilder sb = new StringBuilder();
        sb.append("¡Hola! Puedo ayudarte con información sobre los jugadores. ");
        sb.append("Aquí hay algunas cosas que puedes preguntarme:\n\n");
        sb.append("🔹 Información de un jugador específico\n");
        sb.append("🔹 Jugadores por posición (porteros, defensas, centrocampistas, delanteros)\n");
        sb.append("🔹 Información general de la plantilla\n");
        sb.append("🔹 Información sobre edades\n\n");

        if (!jugadores.isEmpty()) {
            sb.append("📋 Algunos jugadores de nuestra plantilla: ");
            sb.append(jugadores.stream()
                .limit(3)
                .map(Jugador::getNombre)
                .collect(Collectors.joining(", ")));
        }

        return sb.toString();
    }

    private String getYouTubeEmbedUrl(Jugador jugador) {
        return youTubeService.buscarVideoJugador(jugador.getNombre());
    }

    public List<ChatMessage> getHistory(String sessionId) {
        return chatRepo.findBySessionIdOrderByTimestampAsc(sessionId);
    }
}

