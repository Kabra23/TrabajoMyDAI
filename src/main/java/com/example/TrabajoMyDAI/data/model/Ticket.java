package com.example.TrabajoMyDAI.data.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Entity
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_ticket;
    private Long id_usuario;
    private Long id_evento;
    private double precio;
    private Long asiento;

    //Getter y setters

    //Tickets
    public Long getId_ticket(){
        return id_ticket;
    }

    public void setId_ticket(Long id_ticket) {
        this.id_ticket = id_ticket;

    }

    //Usuario
    public Long getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Long id_usuario) {
        this.id_usuario = id_usuario;
    }

    //Evento
    public Long getId_evento() {
        return id_evento;
    }

    public void setId_evento(Long id_evento) {
        this.id_evento = id_evento;
    }


    //Precio

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }


    //Asiento

    public double getAsiento() {
        return asiento;
    }

    public void setAsiento(long asiento) {
        this.asiento = asiento;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id_ticket=" + id_ticket +
                ", id_usuario=" + id_usuario +
                ", id_evento=" + id_evento +
                ", precio=" + precio +
                ", asiento=" + asiento +
                '}';
    }


}
