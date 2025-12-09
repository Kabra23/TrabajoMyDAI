package com.example.TrabajoMyDAI.data.model;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;


@Entity
public class Recordatorio
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "evento_id")
    private Evento evento;
    private String mensaje;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fecha;


    // Getters y setters
    public Long getId_recordatorio() {
        return id;
    }

    public void setId_recordatorio(Long id_recordatorio) {
        this.id = id_recordatorio;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario id_usuario) {
        this.usuario = id_usuario;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento id_evento) {
        this.evento = id_evento;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "Recordatorio{" +
                "id_recordatorio=" + id +
                ", id_usuario=" + (usuario != null ? usuario.getDni() : null) +
                ", id_evento=" + (evento != null ? evento.getId() : null) +
                ", mensaje='" + mensaje + '\'' +
                ", fecha=" + fecha +
                '}';
    }
}
