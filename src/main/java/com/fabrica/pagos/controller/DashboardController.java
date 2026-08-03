package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.Empleado;
import com.fabrica.pagos.repository.EmpleadoRepository;
import com.fabrica.pagos.repository.NominaRepository;
import com.fabrica.pagos.repository.ReciboRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;

@Controller
public class DashboardController {

    private final EmpleadoRepository empleadoRepository;
    private final NominaRepository nominaRepository;
    private final ReciboRepository reciboRepository;

    public DashboardController(EmpleadoRepository empleadoRepository,
                               NominaRepository nominaRepository,
                               ReciboRepository reciboRepository) {
        this.empleadoRepository = empleadoRepository;
        this.nominaRepository = nominaRepository;
        this.reciboRepository = reciboRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalEmpleados", empleadoRepository.count());
        model.addAttribute("totalEmpleadosActivos", empleadoRepository.countActivos());

        long totalNominas = nominaRepository.count();
        model.addAttribute("totalNominas", totalNominas);

        BigDecimal totalPagado = BigDecimal.ZERO;
        var nominas = nominaRepository.findAllByOrderByFechaGeneracionDesc();
        if (!nominas.isEmpty()) {
            var ultima = nominas.get(0);
            model.addAttribute("ultimaNomina", ultima);
            for (var r : reciboRepository.findByNominaIdOrderByEmpleadoCodigoAsc(ultima.getId())) {
                totalPagado = totalPagado.add(r.getSalarioNeto());
            }
        }
        model.addAttribute("ultimoTotalPagado", totalPagado);
        model.addAttribute("ultimasNominas", nominas.stream().limit(5).toList());
        return "dashboard";
    }
}