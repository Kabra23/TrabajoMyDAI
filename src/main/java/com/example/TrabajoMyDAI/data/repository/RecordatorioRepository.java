// RecordatorioRepository.java
package com.example.TrabajoMyDAI.data.repository;

import com.example.TrabajoMyDAI.data.model.Recordatorio;
import com.example.TrabajoMyDAI.data.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecordatorioRepository extends JpaRepository<Recordatorio, Long> {
    List<Recordatorio> findByUsuario(Usuario usuario);
}
