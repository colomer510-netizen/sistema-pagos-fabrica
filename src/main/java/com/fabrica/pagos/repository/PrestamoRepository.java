package com.fabrica.pagos.repository;

import com.fabrica.pagos.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findAllByOrderByFechaDesc();

    List<Prestamo> findByActivoTrueOrderByFechaDesc();

    List<Prestamo> findByEmpleadoIdAndActivoTrueOrderByFechaDesc(Long empleadoId);
}
