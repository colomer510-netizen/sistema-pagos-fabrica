package com.fabrica.pagos.repository;

import com.fabrica.pagos.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByMaterialIdOrderByFechaDesc(Long materialId);
}
