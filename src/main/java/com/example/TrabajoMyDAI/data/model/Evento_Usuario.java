package com.example.TrabajoMyDAI.data.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "evento_usuario")
public class Evento_Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "evento_id")
    private Evento evento;

    // Constructor vacío y con parámetros si es necesario
    public Evento_Usuario() {}

    public Evento_Usuario(Usuario usuario, Evento evento) {
        this.usuario = usuario;
        this.evento = evento;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Usuario getUsuario() {
        return usuario;
    }
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    public Evento getEvento() {
        return evento;
    }
    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Evento_Usuario that = (Evento_Usuario) o;
        return Objects.equals(id, that.id) && Objects.equals(usuario, that.usuario) && Objects.equals(evento, that.evento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, usuario, evento);
    }

    @Override
    public String toString() {
        return "Evento_Usuario{" +
                "id=" + id +
                ", usuario=" + usuario.getDni() +
                ", evento=" + evento.getId_evento() +
                '}';
    }
}
