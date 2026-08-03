package com.fabrica.pagos.repository;

import com.fabrica.pagos.model.DeduccionAplicada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeduccionAplicadaRepository extends JpaRepository<DeduccionAplicada, Long> {

    List<DeduccionAplicada> findByReciboIdOrderByIdAsc(Long reciboId);
}
