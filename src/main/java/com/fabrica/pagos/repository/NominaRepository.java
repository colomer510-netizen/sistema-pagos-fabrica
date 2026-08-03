package com.fabrica.pagos.repository;

import com.fabrica.pagos.model.Nomina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NominaRepository extends JpaRepository<Nomina, Long> {

    Optional<Nomina> findByNumero(String numero);

    List<Nomina> findAllByOrderByFechaGeneracionDesc();

    Optional<Nomina> findTopByOrderByNumeroDesc();

    boolean existsByPeriodoInicioAndPeriodoFin(LocalDate inicio, LocalDate fin);
}
