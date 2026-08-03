package com.fabrica.pagos.repository;

import com.fabrica.pagos.model.CuentaPagar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface CuentaPagarRepository extends JpaRepository<CuentaPagar, Long> {

    List<CuentaPagar> findAllByOrderByFechaVencimientoDesc();

    @Query("select coalesce(sum(c.monto), 0) from CuentaPagar c where c.estado = 'PENDIENTE'")
    BigDecimal totalPendiente();
}
