package com.example.TrabajoMyDAI.data.model;

import jakarta.persistence.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Entity
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dni;
    private String nombre;
    private String email;

    // Relación con Ticket
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new LinkedList<>();

    // Usuario será owner de la relación ManyToMany con Evento
    @ManyToMany()
    @JoinTable(
        name = "usuario_eventos",
        joinColumns = @JoinColumn(name = "usuario_dni"),
        inverseJoinColumns = @JoinColumn(name = "eventos_id_evento")
    )
    private List<Evento> eventos = new LinkedList<>();


    // helper methods to keep both sides in sync (optional)
    public void addEvento(Evento evento) {
        if (evento == null) return;
        this.eventos.add(evento);
        if (!evento.getUsuarios().contains(this)) {
            evento.getUsuarios().add(this);
        }
    }

    public void removeEvento(Evento evento) {
        if (evento == null) return;
        this.eventos.remove(evento);
        evento.getUsuarios().remove(this);
    }

    public Usuario() {
        if(this.eventos == null){
            this.eventos = new LinkedList<>();
        }
    }

    public Long getDni() {
        return dni;
    }
    public void setDni(Long dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }
    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public List<Evento> getEventos() {
        return eventos;
    }
    public void setEventos(List<Evento> eventos) {
        this.eventos = eventos;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(dni, usuario.dni) && Objects.equals(nombre, usuario.nombre) && Objects.equals(email, usuario.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dni, nombre, email);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "dni=" + dni +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}