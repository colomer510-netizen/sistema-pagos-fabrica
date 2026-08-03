package com.fabrica.pagos.repository;

import com.fabrica.pagos.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    Optional<Empleado> findByCodigo(String codigo);

    List<Empleado> findByActivoTrueOrderByCodigoAsc();

    List<Empleado> findAllByOrderByCodigoAsc();

    @Query("select count(e) from Empleado e where e.activo = true")
    long countActivos();
}
