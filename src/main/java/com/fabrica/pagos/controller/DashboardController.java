package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.Empleado;
import com.fabrica.pagos.model.Nomina;
import com.fabrica.pagos.repository.EmpleadoRepository;
import com.fabrica.pagos.repository.NominaRepository;
import com.fabrica.pagos.repository.ReciboRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        var nominas = nominaRepository.findAllByOrderByFechaGeneracionDesc();
        BigDecimal totalPagado = BigDecimal.ZERO;
        if (!nominas.isEmpty()) {
            Nomina ultima = nominas.get(0);
            model.addAttribute("ultimaNomina", ultima);
            for (var r : reciboRepository.findByNominaIdOrderByEmpleadoCodigoAsc(ultima.getId())) {
                totalPagado = totalPagado.add(r.getSalarioNeto());
            }
        }
        model.addAttribute("ultimoTotalPagado", totalPagado);
        model.addAttribute("ultimasNominas", nominas.stream().limit(5).toList());

        model.addAttribute("nominasGrafico", nominas.stream().limit(6).collect(Collectors.toList()));
        Map<Long, BigDecimal> totalPagadoPorNomina = calcularTotalPagadoPorNomina(nominas.stream().limit(6).toList());
        model.addAttribute("totalPagadoPorNomina", totalPagadoPorNomina);
        model.addAttribute("maxTotalGrafico", totalPagadoPorNomina.values().stream()
                .max(BigDecimal::compareTo).orElse(BigDecimal.ONE));

        Map<String, Long> empleadosPorDepartamento = contarPorDepartamento(empleadoRepository.findAllByOrderByCodigoAsc());
        model.addAttribute("empleadosPorDepartamento", empleadosPorDepartamento);
        model.addAttribute("maxDepartamento", empleadosPorDepartamento.values().stream()
                .max(Long::compareTo).orElse(1L));

        return "dashboard";
    }

    private Map<Long, BigDecimal> calcularTotalPagadoPorNomina(List<Nomina> nominas) {
        Map<Long, BigDecimal> mapa = new LinkedHashMap<>();
        for (Nomina n : nominas) {
            BigDecimal total = BigDecimal.ZERO;
            for (var r : reciboRepository.findByNominaIdOrderByEmpleadoCodigoAsc(n.getId())) {
                total = total.add(r.getSalarioNeto());
            }
            mapa.put(n.getId(), total);
        }
        return mapa;
    }

    private Map<String, Long> contarPorDepartamento(List<Empleado> empleados) {
        return empleados.stream()
                .filter(Empleado::getActivo)
                .collect(Collectors.groupingBy(Empleado::getDepartamento, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }
}
