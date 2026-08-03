package com.fabrica.pagos.controller;

import com.fabrica.pagos.model.Empleado;
import com.fabrica.pagos.repository.AsistenciaRepository;
import com.fabrica.pagos.repository.EmpleadoRepository;
import com.fabrica.pagos.service.ContabilidadService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;

@Controller
@RequestMapping("/contabilidad")
public class ContabilidadController {

    private final EmpleadoRepository empleadoRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final ContabilidadService contabilidadService;

    public ContabilidadController(EmpleadoRepository empleadoRepository,
                                  AsistenciaRepository asistenciaRepository,
                                  ContabilidadService contabilidadService) {
        this.empleadoRepository = empleadoRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.contabilidadService = contabilidadService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("empleados", empleadoRepository.findAllByOrderByCodigoAsc());
        return "contabilidad/listar";
    }

    @GetMapping("/calcular/{id}")
    public String calcular(@PathVariable Long id, Model model) {
        Empleado empleado = empleadoRepository.findById(id).orElseThrow();
        int meses = contabilidadService.mesesDesdeContratacion(empleado);
        int anios = contabilidadService.aniosDesdeContratacion(empleado);

        int horasNormales = empleado.getHorasJornada() * 30;
        int horasExtras = 6;
        BigDecimal bruto = contabilidadService.calcularSalarioBruto(empleado, horasNormales, horasExtras);
        BigDecimal inssLaboral = contabilidadService.calcularInssLaboral(bruto);
        BigDecimal inssPatronal = contabilidadService.calcularInssPatronal(bruto);
        BigDecimal neto = contabilidadService.calcularSalarioNeto(bruto, inssLaboral, BigDecimal.ZERO);

        model.addAttribute("empleado", empleado);
        model.addAttribute("meses", meses);
        model.addAttribute("anios", anios);
        model.addAttribute("horasNormales", horasNormales);
        model.addAttribute("horasExtras", horasExtras);
        model.addAttribute("bruto", bruto);
        model.addAttribute("inssLaboral", inssLaboral);
        model.addAttribute("inssPatronal", inssPatronal);
        model.addAttribute("neto", neto);
        model.addAttribute("vacaciones", contabilidadService.calcularVacaciones(empleado, meses));
        model.addAttribute("aguinaldo", contabilidadService.calcularAguinaldo(empleado, meses));
        model.addAttribute("indemnizacion", contabilidadService.calcularIndemnizacion(empleado, anios));
        model.addAttribute("inssPorcentaje", contabilidadService.getInssLaboral());
        model.addAttribute("inssPatronalPorcentaje", contabilidadService.getInssPatronal());
        return "contabilidad/calcular";
    }

    @ModelAttribute
    public void agregarServicioAlContexto(Model model) {
        // vacío: mantenido para futuras utilidades
    }
}
