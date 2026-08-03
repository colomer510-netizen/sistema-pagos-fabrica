package com.fabrica.pagos.repository;

import com.fabrica.pagos.model.DocumentoEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentoEmpleadoRepository extends JpaRepository<DocumentoEmpleado, Long> {

    List<DocumentoEmpleado> findByEmpleadoIdOrderByFechaSubidaDesc(Long empleadoId);
}
