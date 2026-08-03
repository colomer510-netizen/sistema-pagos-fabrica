package com.fabrica.pagos.repository;

import com.fabrica.pagos.model.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {

    List<MovimientoCaja> findAllByOrderByFechaDesc();

    List<MovimientoCaja> findByFechaBetweenOrderByFechaDesc(LocalDate inicio, LocalDate fin);

    @Query("select coalesce(sum(m.monto), 0) from MovimientoCaja m where m.tipo = :tipo and m.fecha between :inicio and :fin")
    BigDecimal sumByTipoBetween(@Param("tipo") String tipo, @Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    @Query("select coalesce(sum(case when m.tipo = 'INGRESO' then m.monto else -m.monto end), 0) from MovimientoCaja m")
    BigDecimal saldoAcumulado();

    @Query("select m.categoria, coalesce(sum(m.monto), 0) from MovimientoCaja m " +
            "where m.tipo = :tipo and m.fecha between :inicio and :fin group by m.categoria order by m.categoria")
    List<Object[]> sumByCategoriaYTipo(@Param("tipo") String tipo, @Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    @Query("select coalesce(sum(m.monto), 0) from MovimientoCaja m " +
            "where m.tipo = 'EGRESO' and m.categoria = :categoria and m.fecha between :inicio and :fin")
    BigDecimal sumEgresosPorCategoria(@Param("categoria") String categoria,
                                      @Param("inicio") LocalDate inicio,
                                      @Param("fin") LocalDate fin);
}
