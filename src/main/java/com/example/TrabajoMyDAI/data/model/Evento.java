package com.example.TrabajoMyDAI.data.model;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Entity
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_evento;
    private String nombre_evento;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fecha_evento;

    private String lugar_evento;
    private String descripcion_evento;

    @ManyToMany(mappedBy = "eventos")
    private List<Usuario> usuarios = new LinkedList<>();

    private String tipo_evento;

    // Nuevo campo para codigo promocional esperado por Thymeleaf
    private String codigoPromo;

    public Evento() { }

    // Getters y Setters
    public Long getId() { return id_evento; }
    public void setId(Long id_evento) { this.id_evento = id_evento; }

    public String getNombre() { return nombre_evento; }
    public void setNombre(String nombre_evento) { this.nombre_evento = nombre_evento; }

    public LocalDateTime getFecha() { return fecha_evento; }
    public void setFecha(LocalDateTime fecha_evento) { this.fecha_evento = fecha_evento; }

    public String getLugar() { return lugar_evento; }
    public void setLugar_evento(String lugar_evento) { this.lugar_evento = lugar_evento; }

    public String getDescripcion() { return descripcion_evento; }
    public void setDescripcion(String descripcion_evento) { this.descripcion_evento = descripcion_evento; }

    public List<Usuario> getUsuarios() { return usuarios; }
    public void setUsuarios(List<Usuario> usuarios) { this.usuarios = usuarios; }

    public String getTipo() { return tipo_evento; }
    public void setTipo(String tipo_evento) { this.tipo_evento = tipo_evento; }

    public void addUsuario(Usuario usuario) { this.usuarios.add(usuario); }

    // Getter y setter para codigoPromo
    public String getCodigoPromo() { return codigoPromo; }
    public void setCodigoPromo(String codigoPromo) { this.codigoPromo = codigoPromo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evento evento = (Evento) o;
        return Objects.equals(id_evento, evento.id_evento) &&
                Objects.equals(nombre_evento, evento.nombre_evento) &&
                Objects.equals(fecha_evento, evento.fecha_evento) &&
                Objects.equals(lugar_evento, evento.lugar_evento) &&
                Objects.equals(descripcion_evento, evento.descripcion_evento) &&
                Objects.equals(tipo_evento, evento.tipo_evento) &&
                Objects.equals(codigoPromo, evento.codigoPromo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_evento, nombre_evento, fecha_evento, lugar_evento, descripcion_evento, tipo_evento, codigoPromo);
    }

    @Override
    public String toString() {
        return "Evento{" +
                "id_evento=" + id_evento +
                ", nombre_evento='" + nombre_evento + '\'' +
                ", fecha_evento=" + fecha_evento +
                ", lugar_evento='" + lugar_evento + '\'' +
                ", descripcion_evento='" + descripcion_evento + '\'' +
                ", usuarios=" + usuarios +
                ", tipo_evento='" + tipo_evento + '\'' +
                ", codigoPromo='" + codigoPromo + '\'' +
                '}';
    }
}
