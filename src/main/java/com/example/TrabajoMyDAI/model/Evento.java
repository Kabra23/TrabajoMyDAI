package com.example.TrabajoMyDAI.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_evento;
    private String nombre_evento;
    private String fecha_evento;
    private String lugar_evento;
    private String descripcion_evento;
    private Long dni_usuario; // Foreign key to Usuario
    private String tipo_evento;


}