// RecordatorioRepository.java
package com.example.TrabajoMyDAI.repository;

import com.example.TrabajoMyDAI.data.model.Recordatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecordatorioRepository extends CrudRepository<Recordatorio, Long> {
}
