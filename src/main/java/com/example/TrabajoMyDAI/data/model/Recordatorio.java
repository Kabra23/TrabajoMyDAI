package com.example.TrabajoMyDAI.data.model;

import jakarta.persistence.*;


@Entity
public class Recordatorio
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_recordatorio;

    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    private Evento evento;
    private String mensaje;
    private String fecha;


    // Getters y setters
    public Long getId_recordatorio() {
        return id_recordatorio;
    }

    public void setId_recordatorio(Long id_recordatorio) {
        this.id_recordatorio = id_recordatorio;
    }

    public Usuario getUusario() {
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
                ", id_usuario=" + usuario.getDni()+
                ", id_evento=" + evento.getId_evento() +
                ", mensaje=" + mensaje +
                ", fecha=" + fecha +
                '}';
    }
}
