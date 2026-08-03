package com.fabrica.pagos.repository;

import com.fabrica.pagos.model.Nomina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NominaRepository extends JpaRepository<Nomina, Long> {

    Optional<Nomina> findByNumero(String numero);

    List<Nomina> findAllByOrderByFechaGeneracionDesc();

    Optional<Nomina> findTopByOrderByNumeroDesc();

    boolean existsByPeriodoInicioAndPeriodoFin(LocalDate inicio, LocalDate fin);

    List<Nomina> findByPeriodoFinBetween(LocalDate inicio, LocalDate fin);

    @Query("select coalesce(sum(n.totalPagar), 0) from Nomina n where n.periodoFin between :inicio and :fin")
    BigDecimal sumTotalPagarEntre(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
}
