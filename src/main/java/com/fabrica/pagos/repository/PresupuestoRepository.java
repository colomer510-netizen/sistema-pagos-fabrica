package com.fabrica.pagos.repository;

import com.fabrica.pagos.model.Presupuesto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {

    List<Presupuesto> findAllByOrderByPeriodoDesc();

    List<Presupuesto> findByPeriodoOrderByCategoriaAsc(String periodo);

    Optional<Presupuesto> findByCategoriaAndPeriodo(String categoria, String periodo);
}
