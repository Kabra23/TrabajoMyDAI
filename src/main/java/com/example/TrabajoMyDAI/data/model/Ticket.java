package com.example.TrabajoMyDAI.data.model;

import jakarta.persistence.*;


@Entity
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_ticket;

    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    private Evento evento;

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
    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario id_usuario) {
        this.usuario = id_usuario;
    }

    //Evento
    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento id_evento) {
        this.evento = id_evento;
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
                ", id_usuario=" + usuario +
                ", id_evento=" + evento +
                ", precio=" + precio +
                ", asiento=" + asiento +
                '}';
    }


}
