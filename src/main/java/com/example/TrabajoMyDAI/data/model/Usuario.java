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
    private String username;
    private String password;
    private String roles;

    // Relación con Ticket
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new LinkedList<>();

    // Usuario será owner de la relación ManyToMany con Evento
    @ManyToMany
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

    // Método para verificar si el usuario es administrador
    public boolean isAdmin() {
        return this.roles != null && this.roles.contains("ADMIN");
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

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoles() {
        return roles;
    }
    public void setRoles(String roles) {
        this.roles = roles;
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