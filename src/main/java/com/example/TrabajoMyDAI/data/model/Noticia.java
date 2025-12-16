package com.example.TrabajoMyDAI.data.model;

import jakarta.persistence.*;

@Entity
public class Noticia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fecha;
    private String titulo;

    @Column(length = 600)
    private String descripcion;

    @Lob
    private String contenido;

    private String icono;

    private String imagen;

    // Getters/Setters
    public Long getId() { return id; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }
}

