package com.example.TrabajoMyDAI.data.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String posicion;
    private Integer dorsal;
    private Integer edad;
    private String nacionalidad;
    private String imagenUrl;

    public Jugador() {
    }

    public Jugador(String nombre, String posicion, Integer dorsal,
                   Integer edad, String nacionalidad, String imagenUrl) {
        this.nombre = nombre;
        this.posicion = posicion;
        this.dorsal = dorsal;
        this.edad = edad;
        this.nacionalidad = nacionalidad;
        this.imagenUrl = imagenUrl;
    }

    // ========= GETTERS Y SETTERS =========
    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPosicion() {
        return posicion;
    }

    public void setPosicion(String posicion) {
        this.posicion = posicion;
    }

    public Integer getDorsal() {
        return dorsal;
    }

    public void setDorsal(Integer dorsal) {
        this.dorsal = dorsal;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
}
