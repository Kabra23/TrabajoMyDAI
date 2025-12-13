package com.example.TrabajoMyDAI.controllers;

import com.example.TrabajoMyDAI.chat.ChatMessage;
import com.example.TrabajoMyDAI.data.model.Jugador;
import com.example.TrabajoMyDAI.data.repository.JugadorRepository;
import com.example.TrabajoMyDAI.data.services.ChatService;
import com.example.TrabajoMyDAI.data.services.PlantillaChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;
    private final PlantillaChatService plantillaChatService;
    private final JugadorRepository jugadorRepository;

    public ChatController(ChatService chatService, PlantillaChatService plantillaChatService, JugadorRepository jugadorRepository) {
        this.chatService = chatService;
        this.plantillaChatService = plantillaChatService;
        this.jugadorRepository = jugadorRepository;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> body) {
        try {
            String sessionId = body.getOrDefault("sessionId", "default");
            String message = body.get("message");
            if (message == null || message.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "message required"));
            }
            String reply = chatService.handleUserMessage(sessionId, message);
            return ResponseEntity.ok(Map.of("reply", reply));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno"));
        }
    }

    @PostMapping("/plantilla/send")
    public ResponseEntity<?> sendPlantillaMessage(@RequestBody Map<String, String> body) {
        try {
            String sessionId = body.getOrDefault("sessionId", "plantilla_default");
            String message = body.get("message");
            if (message == null || message.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "message required"));
            }
            Map<String, Object> response = plantillaChatService.handlePlayerQuestion(sessionId, message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    @GetMapping("/plantilla/jugadores")
    public ResponseEntity<?> getJugadores() {
        try {
            List<Map<String, Object>> jugadores = StreamSupport
                .stream(jugadorRepository.findAll().spliterator(), false)
                .map(j -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", j.getId());
                    map.put("nombre", j.getNombre());
                    map.put("posicion", j.getPosicion());
                    map.put("dorsal", j.getDorsal());
                    map.put("edad", j.getEdad());
                    map.put("nacionalidad", j.getNacionalidad());
                    return map;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(jugadores);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error al obtener jugadores"));
        }
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> history(@PathVariable String sessionId) {
        return ResponseEntity.ok(chatService.getHistory(sessionId));
    }

    @GetMapping("/plantilla/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> plantillaHistory(@PathVariable String sessionId) {
        return ResponseEntity.ok(plantillaChatService.getHistory(sessionId));
    }
}
