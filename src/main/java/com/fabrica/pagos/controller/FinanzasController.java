package com.fabrica.pagos.controller;

import com.fabrica.pagos.repository.MovimientoCajaRepository;
import com.fabrica.pagos.repository.NominaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Controller
@RequestMapping("/finanzas")
public class FinanzasController {

    private final MovimientoCajaRepository movimientoRepository;
    private final NominaRepository nominaRepository;

    public FinanzasController(MovimientoCajaRepository movimientoRepository,
                              NominaRepository nominaRepository) {
        this.movimientoRepository = movimientoRepository;
        this.nominaRepository = nominaRepository;
    }

    @GetMapping
    public String estado(@RequestParam(required = false) String mes, Model model) {
        YearMonth ym = (mes == null || mes.isBlank()) ? YearMonth.now() : YearMonth.parse(mes);
        LocalDate inicio = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();

        BigDecimal ingresos = movimientoRepository.sumByTipoBetween("INGRESO", inicio, fin);
        BigDecimal egresos = movimientoRepository.sumByTipoBetween("EGRESO", inicio, fin);
        BigDecimal nominaMes = nominaRepository.sumTotalPagarEntre(inicio, fin);
        BigDecimal resultado = ingresos.subtract(egresos).subtract(nominaMes);

        model.addAttribute("mes", ym.toString());
        model.addAttribute("ingresos", ingresos);
        model.addAttribute("egresos", egresos);
        model.addAttribute("nomina", nominaMes);
        model.addAttribute("resultado", resultado);
        model.addAttribute("ingresosPorCategoria", movimientoRepository.sumByCategoriaYTipo("INGRESO", inicio, fin));
        model.addAttribute("egresosPorCategoria", movimientoRepository.sumByCategoriaYTipo("EGRESO", inicio, fin));
        return "finanzas/estado";
    }
}
