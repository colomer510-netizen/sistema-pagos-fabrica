package com.fabrica.pagos.repository;

import com.fabrica.pagos.model.Liquidacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LiquidacionRepository extends JpaRepository<Liquidacion, Long> {

    List<Liquidacion> findAllByOrderByFechaSalidaDesc();

    List<Liquidacion> findByEmpleadoIdOrderByFechaSalidaDesc(Long empleadoId);
}
