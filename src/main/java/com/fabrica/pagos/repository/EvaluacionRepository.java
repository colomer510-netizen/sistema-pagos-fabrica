package com.fabrica.pagos.repository;

import com.fabrica.pagos.model.EvaluacionDesempeno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluacionRepository extends JpaRepository<EvaluacionDesempeno, Long> {

    List<EvaluacionDesempeno> findAllByOrderByFechaEvaluacionDesc();

    List<EvaluacionDesempeno> findByEmpleadoIdOrderByFechaEvaluacionDesc(Long empleadoId);
}
