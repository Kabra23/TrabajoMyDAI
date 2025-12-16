package com.example.TrabajoMyDAI.data.repository;

import com.example.TrabajoMyDAI.data.model.Noticia;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NoticiaRepository extends CrudRepository<Noticia, Long> {
    List<Noticia> findAllByOrderByIdDesc();
}

