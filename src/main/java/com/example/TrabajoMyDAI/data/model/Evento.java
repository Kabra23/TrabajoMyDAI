package com.example.TrabajoMyDAI.data.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_evento;
    private String nombre_evento;
    private String fecha_evento;
    private String lugar_evento;
    private String descripcion_evento;
    @ManyToOne
    private Usuario usuario; // Foreign key to Usuario
    private String tipo_evento;


    public Long getId_evento() {
        return id_evento;
    }
    public void setId_evento(Long id_evento) {
        this.id_evento = id_evento;
    }
    public String getNombre_evento() {
        return nombre_evento;
    }
    public void setNombre_evento(String nombre_evento) {
        this.nombre_evento = nombre_evento;
    }
    public String getFecha_evento() {
        return fecha_evento;
    }
    public void setFecha_evento(String fecha_evento) {
        this.fecha_evento = fecha_evento;
    }
    public String getLugar_evento() {
        return lugar_evento;
    }
    public void setLugar_evento(String lugar_evento) {
        this.lugar_evento = lugar_evento;
    }
    public String getDescripcion_evento() {
        return descripcion_evento;
    }
    public void setDescripcion_evento(String descripcion_evento) {
        this.descripcion_evento = descripcion_evento;
    }
    public Usuario getUsuario() {
        return usuario;
    }
    public void setUsuario(Usuario dni_usuario) {
        this.usuario = dni_usuario;
    }
    public String getTipo_evento() {
        return tipo_evento;
    }
    public void setTipo_evento(String tipo_evento) {
        this.tipo_evento = tipo_evento;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Evento evento = (Evento) o;
        return Objects.equals(id_evento, evento.id_evento) && Objects.equals(nombre_evento, evento.nombre_evento) && Objects.equals(fecha_evento, evento.fecha_evento) && Objects.equals(lugar_evento, evento.lugar_evento) && Objects.equals(descripcion_evento, evento.descripcion_evento) && Objects.equals(usuario.getDni(), evento.getUsuario().getDni()) && Objects.equals(tipo_evento, evento.tipo_evento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_evento, nombre_evento, fecha_evento, lugar_evento, descripcion_evento, usuario.getDni(), tipo_evento);
    }

    @Override
    public String toString() {
        return "Evento{" +
                "id_evento=" + id_evento +
                ", nombre_evento='" + nombre_evento + '\'' +
                ", fecha_evento='" + fecha_evento + '\'' +
                ", lugar_evento='" + lugar_evento + '\'' +
                ", descripcion_evento='" + descripcion_evento + '\'' +
                ", dni_usuario=" + usuario.getDni() +
                ", tipo_evento='" + tipo_evento + '\'' +
                '}';
    }
}