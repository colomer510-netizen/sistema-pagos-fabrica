package com.fabrica.pagos.repository;

import com.fabrica.pagos.model.DeduccionConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeduccionConfigRepository extends JpaRepository<DeduccionConfig, Long> {

    List<DeduccionConfig> findByActivaTrueOrderByNombreAsc();

    List<DeduccionConfig> findAllByOrderByNombreAsc();

    List<DeduccionConfig> findByNombreContainingIgnoreCase(String nombre);
}
