package com.example.TrabajoMyDAI.data.model;

import jakarta.persistence.*;

@Entity
@Table(name = "evento_usuario")
public class Evento_Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    // Constructor vacío y con parámetros si es necesario
    public Evento_Usuario() {}

    public Evento_Usuario(Usuario usuario, Evento evento) {
        this.usuario = usuario;
        this.evento = evento;
    }
}
