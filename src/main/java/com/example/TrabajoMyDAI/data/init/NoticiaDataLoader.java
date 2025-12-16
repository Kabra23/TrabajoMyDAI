package com.example.TrabajoMyDAI.data.init;

import com.example.TrabajoMyDAI.data.model.Noticia;
import com.example.TrabajoMyDAI.data.repository.NoticiaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class NoticiaDataLoader implements CommandLineRunner {

    private final NoticiaRepository repo;

    public NoticiaDataLoader(NoticiaRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;

        // Noticia 1
        repo.save(crear("Noviembre 2025", "Ibrahim Diarra operado con éxito",
                "Ibrahim Diarra fue operado por una rotura muscular en el muslo derecho. Se espera que esté fuera aproximadamente cinco meses.",
                "Parte médico:\n" +
                        "- Intervención: realizada con éxito.\n" +
                        "- Lesión: rotura muscular en el muslo derecho.\n" +
                        "- Tiempo estimado de baja: ~5 meses.\n\n" +

                        "Evolución y seguimiento:\n" +
                        "- El jugador queda pendiente de controles médicos periódicos.\n" +
                        "- Se priorizará una recuperación completa antes de volver a competir.\n\n" +

                        "Impacto en el equipo:\n" +
                        "- El cuerpo técnico ajustará cargas y rotaciones para cubrir su ausencia.\n" +
                        "- La plantilla afrontará el calendario con foco en la continuidad del rendimiento.\n\n" +

                        "Plan de recuperación (aprox.):\n" +
                        "- Fase 1: reposo relativo y control del dolor.\n" +
                        "- Fase 2: fisioterapia y recuperación de movilidad.\n" +
                        "- Fase 3: readaptación física progresiva.\n" +
                        "- Fase 4: trabajo específico en campo y reintegración al grupo.\n\n" +

                        "Próximos pasos:\n" +
                        "- Inicio inmediato del trabajo de readaptación con el staff médico.\n" +
                        "- Revisión de evolución antes de aumentar intensidad de entrenamientos.\n\n" +

                        "Mensaje del club:\n" +
                        "El jugador iniciará su readaptación de forma progresiva junto al cuerpo médico, respetando los tiempos para un regreso seguro.",
                "🏥",
                "ibrahim.jpg"
        ));



        // Noticia 2
        repo.save(crear("16 Noviembre", "Espanyol B 1-0 Barça Atlètic",
                "El equipo perdió el derbi local en un partido difícil en el Estadio Dani Jarque. Una derrota que deja al equipo con ganas de revancha.",
                "Crónica del partido:\n" +
                        "El Barça Atlètic cayó por la mínima en un derbi muy trabajado, con ritmo alto y pocos espacios.\n\n" +

                        "Cómo se desarrolló:\n" +
                        "- Primera parte muy igualada, con duelos intensos y pocas concesiones.\n" +
                        "- En la segunda mitad, el partido se abrió por momentos, pero el marcador no acompañó.\n\n" +

                        "Puntos clave:\n" +
                        "- Partido cerrado, con pocas ocasiones claras.\n" +
                        "- El equipo lo intentó hasta el final, pero faltó acierto en el último pase.\n" +
                        "- Buen orden defensivo en fases, aunque pequeños detalles marcaron la diferencia.\n\n" +

                        "Lecturas del cuerpo técnico:\n" +
                        "- Mantener la calma en partidos de máxima tensión.\n" +
                        "- Mejorar la toma de decisiones en el último tercio.\n\n" +

                        "Lo que viene:\n" +
                        "El objetivo ahora es corregir detalles, recuperar sensaciones y volver a sumar en el próximo encuentro.",
                "❌",
                "espanyol.jpg"));

// Noticia 3
        repo.save(crear("9 Noviembre", "Barça Atlètic 4-1 Torrent",
                "Victoria cómoda que mueve al equipo al segundo lugar de la clasificación. Gran actuación del conjunto azulgrana en casa.",
                "Resumen del partido:\n" +
                        "El Barça Atlètic firmó una victoria convincente en casa, con ritmo alto, verticalidad y control del juego en los momentos clave.\n\n" +

                        "Claves del 4-1:\n" +
                        "- Intensidad desde el inicio para dominar territorio.\n" +
                        "- Circulación rápida para encontrar ventajas entre líneas.\n" +
                        "- Eficacia en el área rival para convertir las ocasiones.\n\n" +

                        "Lo más destacado:\n" +
                        "- Buena salida desde el inicio.\n" +
                        "- Eficacia en el área rival.\n" +
                        "- Gestión del resultado en la segunda parte.\n\n" +

                        "Impacto en la clasificación:\n" +
                        "- Los tres puntos impulsan al equipo a puestos altos.\n" +
                        "- Refuerza confianza y dinámica del grupo.\n\n" +

                        "Próximo objetivo:\n" +
                        "Mantener la regularidad y el nivel competitivo para seguir peleando arriba.",
                "🎉",
                "torrent.jpg"));

// Noticia 4
        repo.save(crear("2 Noviembre", "Girona B 0-0 Barça Atlètic",
                "Ambos equipos sumaron un punto. El Barça Atlètic perdió el primer lugar tras este empate sin goles en Girona.",
                "Crónica del partido:\n" +
                        "Duelo muy táctico y con poco margen para el error. Ambos equipos priorizaron el orden y minimizar riesgos.\n\n" +

                        "Detalles del 0-0:\n" +
                        "- Partido equilibrado en el medio campo.\n" +
                        "- El equipo defendió bien y mantuvo la portería a cero.\n" +
                        "- Faltó claridad y punch en el último tercio.\n\n" +

                        "Qué funcionó:\n" +
                        "- Solidez defensiva y buena coordinación sin balón.\n" +
                        "- Capacidad para sostener el ritmo en un campo exigente.\n\n" +

                        "Qué mejorar:\n" +
                        "- Más precisión en el pase final.\n" +
                        "- Generar segundas jugadas cerca del área rival.\n\n" +

                        "Lectura final:\n" +
                        "El punto suma, pero el equipo buscará recuperar el liderato cuanto antes con una victoria en la próxima jornada.",
                "⚖️",
                "girona.jpg"));

// Noticia 5
        repo.save(crear("26 Octubre", "Empate 2-2 con compromiso",
                "El Barça Atlètic empató 2-2 a pesar de la expulsión de Joan Anaya. El equipo mostró carácter y compromiso en inferioridad numérica.",
                "Resumen del partido:\n" +
                        "El Barça Atlètic rescató un empate de mucho mérito en un partido de máxima exigencia, condicionado por jugar en inferioridad durante buena parte del encuentro.\n\n" +

                        "Claves:\n" +
                        "- Expulsión que condicionó el ritmo y obligó a ajustar el plan.\n" +
                        "- Gran esfuerzo defensivo para sostener el resultado.\n" +
                        "- Personalidad y carácter para competir con un jugador menos.\n\n" +

                        "Cómo lo sostuvo el equipo:\n" +
                        "- Repliegue ordenado y ayudas constantes.\n" +
                        "- Gestión emocional para no perder la concentración.\n" +
                        "- Transiciones para seguir siendo amenaza cuando hubo espacio.\n\n" +

                        "Conclusión:\n" +
                        "Un punto que refuerza la mentalidad del grupo y su capacidad de reacción en escenarios adversos.",
                "🟥",
                "empate.jpg"));

// Noticia 6
        repo.save(crear("19 Octubre", "Derrota 3-2 en Camp d'Esport",
                "A pesar del buen esfuerzo del equipo, cayeron derrotados 3-2 en el Camp d'Esport. El conjunto mostró actitud pese al resultado.",
                "Crónica del partido:\n" +
                        "Partido abierto y con alternativas, donde el Barça Atlètic compitió hasta el final. El marcador se decidió por detalles en momentos puntuales.\n\n" +

                        "Lo más importante:\n" +
                        "- El equipo compitió hasta el final y no bajó los brazos.\n" +
                        "- Se generaron ocasiones, pero faltó cerrar el partido en las áreas.\n" +
                        "- Pequeños desajustes defensivos marcaron la diferencia.\n\n" +

                        "Qué deja el partido:\n" +
                        "- Buenas fases de juego y valentía con balón.\n" +
                        "- Aprendizajes claros para ajustar concentración y balance defensivo.\n\n" +

                        "Próximo paso:\n" +
                        "Toca aprender de la derrota, recuperar confianza y enfocarse en el siguiente compromiso con mentalidad de mejora.",
                "😔",
                "camp d'sports.jpeg"));

// Noticia 7
        repo.save(crear("Octubre 2025", "Renovación de Landry hasta 2028",
                "El contrato de Landry fue extendido hasta 2028. Una gran noticia para el futuro del Barça Atlètic y la apuesta por el talento joven.",
                "Comunicado:\n" +
                        "El club y el jugador han llegado a un acuerdo para extender el contrato hasta 2028, consolidando un proyecto que apuesta por la continuidad y el desarrollo.\n\n" +

                        "Por qué es importante:\n" +
                        "- Refuerza el proyecto deportivo a medio plazo.\n" +
                        "- Se asegura continuidad de talento joven.\n" +
                        "- Mensaje claro de confianza y crecimiento dentro del club.\n\n" +

                        "Qué significa para el equipo:\n" +
                        "- Estabilidad en la planificación de la plantilla.\n" +
                        "- Competencia sana y evolución progresiva dentro del vestuario.\n\n" +

                        "Mirando al futuro:\n" +
                        "La renovación confirma la apuesta por formar jugadores con recorrido y proyección, alineados con la identidad del Barça Atlètic.",
                "✍️",
                "landry.jpg"));

// Noticia 8
        repo.save(crear("Octubre 2025", "Alexis Olmedo operado con éxito",
                "Alexis Olmedo sufrió una rotura parcial de menisco y fue operado con éxito. El jugador trabajará para volver lo antes posible.",
                "Parte médico:\n" +
                        "- Lesión: rotura parcial de menisco.\n" +
                        "- Operación: realizada con éxito.\n\n" +

                        "Evolución y recuperación:\n" +
                        "- Seguimiento médico y fisioterapia desde los primeros días.\n" +
                        "- Progresión por fases según sensaciones y controles.\n\n" +

                        "Objetivo del proceso:\n" +
                        "- Regresar de forma segura y progresiva.\n" +
                        "- Priorizar la recuperación completa por encima de las prisas.\n\n" +

                        "Mensaje:\n" +
                        "El jugador comenzará su recuperación siguiendo las pautas del cuerpo médico, respetando los tiempos establecidos.",
                "🏥",
                "olmedo.jpg"));

// Noticia 9
        repo.save(crear("Noviembre 2025", "Belletti habla sobre el equipo",
                "El entrenador habló sobre cómo la plantilla está trabajando duro para recuperarse tras las derrotas y mantener actuaciones sólidas.",
                "Declaraciones del entrenador:\n" +
                        "Belletti valoró el momento del equipo y destacó el trabajo diario como base para recuperar la mejor versión.\n\n" +

                        "Ideas principales:\n" +
                        "- Confianza en el proceso y en la evolución del grupo.\n" +
                        "- Importancia de mantener la solidez en ambas áreas.\n" +
                        "- Aprender de los errores sin perder identidad competitiva.\n\n" +

                        "Mensaje a la afición:\n" +
                        "- Unidad y constancia para volver a la senda de la victoria.\n" +
                        "- Enfoque total en el siguiente partido.\n\n" +

                        "Conclusión:\n" +
                        "El mensaje es claro: unión, trabajo y constancia para convertir el esfuerzo en resultados.",
                "💪",
                "belleti.jpg"));

// Noticia 10
        repo.save(crear("Próximamente", "Próximo partido vs Terrassa FC",
                "El siguiente partido es contra Terrassa FC. El equipo está determinado a volver a ganar en el Estadi Johan Cruyff.",
                "Previa del partido:\n" +
                        "El Barça Atlètic ya prepara un encuentro importante ante Terrassa FC, con el objetivo de volver a sumar de tres y reforzar la dinámica del equipo.\n\n" +

                        "Qué esperar:\n" +
                        "- Partido intenso y muy disputado.\n" +
                        "- Ritmo alto y duelos constantes en medio campo.\n" +
                        "- Detalles en las áreas como factor decisivo.\n\n" +

                        "Objetivos del Barça Atlètic:\n" +
                        "- Volver a sumar de tres.\n" +
                        "- Mantener la solidez defensiva.\n" +
                        "- Ser más determinantes en el último tercio.\n\n" +

                        "Factor clave:\n" +
                        "- Apoyo de la afición en el Estadi Johan Cruyff.\n\n" +

                        "Cierre:\n" +
                        "La plantilla llega enfocada y con ganas de darle una alegría a la afición.",
                "🔜",
                "Terrasa.jpg"));

    }

    private Noticia crear(String fecha, String titulo, String descripcion, String contenido, String icono, String imagen) {
        Noticia n = new Noticia();
        n.setFecha(fecha);
        n.setTitulo(titulo);
        n.setDescripcion(descripcion);
        n.setContenido(contenido);
        n.setIcono(icono);
        n.setImagen(imagen);
        return n;
    }
}


