package com.fabrica.pagos.repository;

import com.fabrica.pagos.model.PermisoVacacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PermisoRepository extends JpaRepository<PermisoVacacion, Long> {

    List<PermisoVacacion> findAllByOrderByFechaInicioDesc();

    List<PermisoVacacion> findByEmpleadoIdOrderByFechaInicioDesc(Long empleadoId);

    List<PermisoVacacion> findByEmpleadoIdAndTipoAndEstadoAndFechaFinLessThanEqual(
            Long empleadoId, String tipo, String estado, LocalDate fechaFin);

    List<PermisoVacacion> findByEmpleadoIdAndTipoAndEstado(Long empleadoId, String tipo, String estado);
}
