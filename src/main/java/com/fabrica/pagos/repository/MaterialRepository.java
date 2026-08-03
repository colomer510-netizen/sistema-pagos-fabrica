package com.fabrica.pagos.repository;

import com.fabrica.pagos.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findAllByOrderByNombreAsc();

    @Query("select m from Material m where m.stockActual <= m.stockMinimo order by m.nombre asc")
    List<Material> findByBajoStock();

    @Query("select count(m) from Material m where m.stockActual <= m.stockMinimo")
    long countBajoStock();

    @Query("select coalesce(sum(m.stockActual * m.precioUnitario), 0) from Material m")
    java.math.BigDecimal sumValorInventario();

    Optional<Material> findByCodigoIgnoreCase(String codigo);
}
