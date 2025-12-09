package com.example.TrabajoMyDAI.controllers;
import com.example.TrabajoMyDAI.data.model.Jugador;
import com.example.TrabajoMyDAI.data.repository.JugadorRepository;
import com.example.TrabajoMyDAI.data.services.PollinationsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Controller
@RequestMapping("/jugadores")
public class JugadorController {

    @Autowired
    private PollinationsService pollinationsService;

    @Autowired
    private JugadorRepository jugadorRepository;

    private final ObjectMapper objectMapper;

    public JugadorController() {
        this.objectMapper = new ObjectMapper();
        // Configurar para escapar caracteres no-ASCII
        this.objectMapper.getFactory().configure(
            com.fasterxml.jackson.core.JsonGenerator.Feature.ESCAPE_NON_ASCII, true
        );
    }

    @GetMapping("/{id}/foto")
    public String mostrarFormularioFoto(@PathVariable Long id, Model model) {
        Optional<Jugador> jugadorOpt = jugadorRepository.findById(id);
        if (jugadorOpt.isEmpty()) {
            return "redirect:/plantilla";
        }

        Jugador jugador = jugadorOpt.get();
        model.addAttribute("jugador", jugador);
        return "foto-jugador";
    }

    @PostMapping("/{id}/generar-foto")
    @ResponseBody
    public ResponseEntity<String> generarFotoConJugador(
            @PathVariable Long id,
            @RequestParam("foto") MultipartFile foto) {

        try {
            // Validar tamaño del archivo
            if (foto.isEmpty()) {
                return crearRespuestaError(HttpStatus.BAD_REQUEST, "No se ha seleccionado ningún archivo");
            }

            // Validar tipo de archivo
            String contentType = foto.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return crearRespuestaError(HttpStatus.BAD_REQUEST, "El archivo debe ser una imagen");
            }

            Optional<Jugador> jugadorOpt = jugadorRepository.findById(id);
            if (jugadorOpt.isEmpty()) {
                return crearRespuestaError(HttpStatus.NOT_FOUND, "Jugador no encontrado");
            }

            Jugador jugador = jugadorOpt.get();

            System.out.println("=== GENERANDO FOTO CON JUGADOR ===");
            System.out.println("Jugador: " + jugador.getNombre() + " (#" + jugador.getDorsal() + ")");

            try {
                // Generar imagen directamente con Pollinations.AI
                String imagenJugadorUrl = jugador.getImagenUrl();
                System.out.println("URL imagen jugador: " + imagenJugadorUrl);

                String imageUrl = pollinationsService.generarImagenCombinada(
                    null, // No necesitamos descripción de Gemini
                    imagenJugadorUrl,
                    jugador.getNombre(),
                    jugador.getDorsal()
                );
                System.out.println("✓ URL de imagen generada: " + imageUrl);

                // Construir respuesta simple solo con la imagen
                com.fasterxml.jackson.databind.node.ObjectNode respuesta = objectMapper.createObjectNode();
                respuesta.put("imageUrl", imageUrl);
                respuesta.put("imageGenerated", true);
                respuesta.put("jugadorNombre", jugador.getNombre());
                respuesta.put("jugadorDorsal", jugador.getDorsal());

                String respuestaFinal = objectMapper.writeValueAsString(respuesta);
                System.out.println("✓ Imagen generada exitosamente");
                System.out.println("=== FIN GENERACION ===");

                return ResponseEntity.ok()
                        .header("Content-Type", "application/json; charset=UTF-8")
                        .body(respuestaFinal);

            } catch (Exception ex) {
                System.err.println("✗ Error al generar imagen:");
                System.err.println("Mensaje: " + ex.getMessage());
                ex.printStackTrace();

                return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al generar imagen: " + limpiarMensajeError(ex.getMessage()));
            }

        } catch (org.springframework.web.multipart.MaxUploadSizeExceededException e) {
            return crearRespuestaError(HttpStatus.PAYLOAD_TOO_LARGE, "El archivo es demasiado grande. Máximo 10MB");
        } catch (Exception e) {
            e.printStackTrace();
            return crearRespuestaError(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error inesperado: " + limpiarMensajeError(e.getMessage()));
        }
    }

    private ResponseEntity<String> crearRespuestaError(HttpStatus status, String mensaje) {
        try {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", mensaje);
            return ResponseEntity.status(status)
                    .header("Content-Type", "application/json")
                    .body(objectMapper.writeValueAsString(errorNode));
        } catch (Exception e) {
            // Fallback en caso de error al serializar
            return ResponseEntity.status(status)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"Error interno del servidor\"}");
        }
    }

    private String limpiarMensajeError(String mensaje) {
        if (mensaje == null) {
            return "Error desconocido";
        }
        // Remover caracteres de control y limpiar el mensaje
        return mensaje
                .replaceAll("[\\r\\n\\t]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
