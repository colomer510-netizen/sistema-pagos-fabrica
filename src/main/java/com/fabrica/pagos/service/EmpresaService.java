package com.fabrica.pagos.service;

import com.fabrica.pagos.model.Empresa;
import com.fabrica.pagos.repository.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public Empresa obtener() {
        return empresaRepository.findTopByOrderByIdAsc().orElseGet(() -> {
            Empresa e = new Empresa();
            e.setNombre("Mi Empresa");
            e.setMoneda("C$");
            return empresaRepository.save(e);
        });
    }

    @Transactional
    public Empresa guardar(Empresa empresa) {
        empresa.setFechaActualizacion(java.time.LocalDateTime.now());
        return empresaRepository.save(empresa);
    }
}
