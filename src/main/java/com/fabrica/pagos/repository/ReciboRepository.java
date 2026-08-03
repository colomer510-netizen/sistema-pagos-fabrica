package com.fabrica.pagos.repository;

import com.fabrica.pagos.model.Recibo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReciboRepository extends JpaRepository<Recibo, Long> {

    List<Recibo> findByNominaIdOrderByEmpleadoCodigoAsc(Long nominaId);

    List<Recibo> findByEmpleadoIdOrderByNominaFechaGeneracionDesc(Long empleadoId);
}