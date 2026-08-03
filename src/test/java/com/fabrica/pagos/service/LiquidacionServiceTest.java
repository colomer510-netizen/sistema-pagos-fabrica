package com.fabrica.pagos.service;

import com.fabrica.pagos.model.Empleado;
import com.fabrica.pagos.model.Liquidacion;
import com.fabrica.pagos.model.PermisoVacacion;
import com.fabrica.pagos.repository.PermisoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiquidacionServiceTest {

    @Mock
    private PermisoRepository permisoRepository;

    private LiquidacionService service;

    private Empleado empleado;

    @BeforeEach
    void setUp() {
        service = new LiquidacionService(permisoRepository);
        ReflectionTestUtils.setField(service, "diasVacacionesPorAnyo", 15);
        ReflectionTestUtils.setField(service, "diasIndemnizacionPorAnyo", 30);
        empleado = new Empleado();
        empleado.setId(1L);
        empleado.setTarifaHora(new BigDecimal("40.00"));
        empleado.setHorasJornada(8);
        empleado.setFechaContratacion(LocalDate.of(2020, 1, 1));
    }

    private void sinPermisos() {
        when(permisoRepository.findByEmpleadoIdAndTipoAndEstadoAndFechaFinLessThanEqual(
                anyLong(), eq("VACACIONES"), eq("APROBADO"), any(LocalDate.class)))
            .thenReturn(List.of());
    }

    @Test
    void calcular_renuncia_soloVacacionesPendientes() {
        sinPermisos();
        Liquidacion l = service.calcular(empleado, LocalDate.of(2024, 1, 1), "RENUNCIA", null, null);
        assertEquals(60, l.getDiasVacacionesPendientes());
        assertEquals(new BigDecimal("19200.00"), l.getMontoVacaciones());
        assertEquals(0, l.getIndemnizacion().compareTo(BigDecimal.ZERO));
        assertEquals(new BigDecimal("19200.00"), l.getTotal());
    }

    @Test
    void calcular_despido_incluyeIndemnizacion() {
        sinPermisos();
        Liquidacion l = service.calcular(empleado, LocalDate.of(2024, 1, 1), "DESPIDO", BigDecimal.ZERO, null);
        assertEquals(new BigDecimal("38400.00"), l.getIndemnizacion());
        assertEquals(new BigDecimal("57600.00"), l.getTotal());
    }

    @Test
    void calcular_restaDiasUsadosAprobados() {
        PermisoVacacion permiso = new PermisoVacacion();
        permiso.setDias(10);
        when(permisoRepository.findByEmpleadoIdAndTipoAndEstadoAndFechaFinLessThanEqual(
                anyLong(), eq("VACACIONES"), eq("APROBADO"), any(LocalDate.class)))
            .thenReturn(List.of(permiso));
        Liquidacion l = service.calcular(empleado, LocalDate.of(2024, 1, 1), "DESPIDO", BigDecimal.ZERO, null);
        assertEquals(50, l.getDiasVacacionesPendientes());
        assertEquals(new BigDecimal("16000.00"), l.getMontoVacaciones());
        assertEquals(new BigDecimal("54400.00"), l.getTotal());
    }

    @Test
    void calcular_sumaOtrosYObservacionAlTotal() {
        sinPermisos();
        Liquidacion l = service.calcular(empleado, LocalDate.of(2024, 1, 1),
                "RENUNCIA", new BigDecimal("500.00"), "Obs prueba");
        assertEquals(new BigDecimal("19700.00"), l.getTotal());
        assertEquals("Obs prueba", l.getObservacion());
    }

    @Test
    void calcular_unMes_generaUnDiaPorRedondeo() {
        sinPermisos();
        Empleado nuevo = new Empleado();
        nuevo.setId(2L);
        nuevo.setTarifaHora(new BigDecimal("40.00"));
        nuevo.setHorasJornada(8);
        nuevo.setFechaContratacion(LocalDate.of(2023, 12, 1));
        Liquidacion l = service.calcular(nuevo, LocalDate.of(2024, 1, 1), "RENUNCIA", null, null);
        assertEquals(1, l.getDiasVacacionesPendientes());
        assertEquals(new BigDecimal("320.00"), l.getMontoVacaciones());
    }
}
