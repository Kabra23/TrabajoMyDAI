package com.example.TrabajoMyDAI.data.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Zona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre; // "Tribuna", "Grada Lateral", "Gol Nord", "Gol Sud"

    @ManyToOne
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    private Integer capacidadTotal; // Número total de entradas disponibles en esta zona

    private Integer entradasVendidas = 0; // Número de entradas ya vendidas

    private Double precio; // Precio base de la zona

    @OneToMany(mappedBy = "zona", cascade = CascadeType.ALL)
    private List<Ticket> tickets = new ArrayList<>();

    // Constructor vacío
    public Zona() {}

    // Constructor con parámetros
    public Zona(String nombre, Evento evento, Integer capacidadTotal) {
        this.nombre = nombre;
        this.evento = evento;
        this.capacidadTotal = capacidadTotal;
        this.entradasVendidas = 0;
    }

    // Constructor con precio
    public Zona(String nombre, Evento evento, Integer capacidadTotal, Double precio) {
        this.nombre = nombre;
        this.evento = evento;
        this.capacidadTotal = capacidadTotal;
        this.entradasVendidas = 0;
        this.precio = precio;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public Integer getCapacidadTotal() {
        return capacidadTotal;
    }

    public void setCapacidadTotal(Integer capacidadTotal) {
        this.capacidadTotal = capacidadTotal;
    }

    public Integer getEntradasVendidas() {
        return entradasVendidas;
    }

    public void setEntradasVendidas(Integer entradasVendidas) {
        this.entradasVendidas = entradasVendidas;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    // Métodos auxiliares
    public Integer getEntradasDisponibles() {
        if (capacidadTotal == null) {
            return null; // Capacidad ilimitada
        }
        return capacidadTotal - (entradasVendidas != null ? entradasVendidas : 0);
    }

    public boolean hayDisponibilidad() {
        if (capacidadTotal == null) {
            return true; // Capacidad ilimitada
        }
        return getEntradasDisponibles() > 0;
    }

    public void incrementarVendidas() {
        if (entradasVendidas == null) {
            entradasVendidas = 0;
        }
        entradasVendidas++;
    }

    public void decrementarVendidas() {
        if (entradasVendidas != null && entradasVendidas > 0) {
            entradasVendidas--;
        }
    }

    @Override
    public String toString() {
        return "Zona{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", capacidadTotal=" + capacidadTotal +
                ", entradasVendidas=" + entradasVendidas +
                ", disponibles=" + getEntradasDisponibles() +
                '}';
    }
}

