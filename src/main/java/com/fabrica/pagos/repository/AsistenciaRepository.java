package com.fabrica.pagos.repository;

import com.fabrica.pagos.model.Asistencia;
import com.fabrica.pagos.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    Optional<Asistencia> findByEmpleadoIdAndFecha(Long empleadoId, LocalDate fecha);

    List<Asistencia> findByFechaBetweenOrderByFechaAsc(LocalDate inicio, LocalDate fin);

    List<Asistencia> findByEmpleadoAndFechaBetweenOrderByFechaAsc(Empleado empleado, LocalDate inicio, LocalDate fin);

    List<Asistencia> findByFechaOrderByEmpleadoCodigoAsc(LocalDate fecha);

    boolean existsByFechaBetween(LocalDate inicio, LocalDate fin);

    @Query("select coalesce(sum(a.horasTrabajadas), 0) from Asistencia a " +
           "where a.empleado = :emp and a.fecha between :inicio and :fin")
    Integer sumHorasPorPeriodo(@Param("emp") Empleado empleado,
                               @Param("inicio") LocalDate inicio,
                               @Param("fin") LocalDate fin);
}