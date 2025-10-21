package com.example.TrabajoMyDAI.data.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class Recordatorio
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_recordatorio;

    private Long id_usuario;
    private Long id_evento;
    private String mensaje;
    private String fecha;


    // Getters y setters
    public Long getId_recordatorio() {
        return id_recordatorio;
    }

    public void setId_recordatorio(Long id_recordatorio) {
        this.id_recordatorio = id_recordatorio;
    }

    public Long getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Long id_usuario) {
        this.id_usuario = id_usuario;
    }

    public Long getId_evento() {
        return id_evento;
    }

    public void setId_evento(Long id_evento) {
        this.id_evento = id_evento;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id_recordatorio=" + id_recordatorio +
                ", id_usuario=" + id_usuario +
                ", id_evento=" + id_evento +
                ", mensaje=" + mensaje +
                ", fecha=" + fecha +
                '}';
    }

}
