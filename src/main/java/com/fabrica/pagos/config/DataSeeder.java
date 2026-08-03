package com.fabrica.pagos.config;

import com.fabrica.pagos.model.DeduccionConfig;
import com.fabrica.pagos.model.Empleado;
import com.fabrica.pagos.model.Usuario;
import com.fabrica.pagos.repository.DeduccionConfigRepository;
import com.fabrica.pagos.repository.EmpleadoRepository;
import com.fabrica.pagos.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final DeduccionConfigRepository deduccionConfigRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean seedEnabled;

    public DataSeeder(UsuarioRepository usuarioRepository,
                      EmpleadoRepository empleadoRepository,
                      DeduccionConfigRepository deduccionConfigRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.seed.enabled:true}") boolean seedEnabled) {
        this.usuarioRepository = usuarioRepository;
        this.empleadoRepository = empleadoRepository;
        this.deduccionConfigRepository = deduccionConfigRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedEnabled = seedEnabled;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNombre("Administrador del Sistema");
            admin.setRol("ADMIN");
            admin.setActivo(true);
            usuarioRepository.save(admin);

            Usuario lector = new Usuario();
            lector.setUsername("lector");
            lector.setPassword(passwordEncoder.encode("lector123"));
            lector.setNombre("Usuario solo lectura");
            lector.setRol("LECTOR");
            lector.setActivo(true);
            usuarioRepository.save(lector);
            log.info("Usuarios creados: admin/admin123 (ADMIN), lector/lector123 (LECTOR)");
        }

        if (empleadoRepository.count() == 0) {
            crearEmpleado("EMP-001", "Juan", "Pérez", "001-010194-0001A", "Operador de máquina", "Producción",
                    "40.00", LocalDate.of(2021, 3, 15));
            crearEmpleado("EMP-002", "María", "López", "001-150395-0002B", "Supervisora de línea", "Producción",
                    "55.00", LocalDate.of(2020, 7, 1));
            crearEmpleado("EMP-003", "Carlos", "García", "001-220491-0003C", "Mecánico", "Mantenimiento",
                    "60.00", LocalDate.of(2019, 1, 10));
            crearEmpleado("EMP-004", "Ana", "Martínez", "001-090988-0004D", "Contadora asistente", "Administración",
                    "70.00", LocalDate.of(2022, 5, 20));
            crearEmpleado("EMP-005", "Luis", "Hernández", "001-301198-0005E", "Bodeguero", "Logística",
                    "45.00", LocalDate.of(2023, 2, 8));
            log.info("Empleados de ejemplo creados (5)");
        }

        if (deduccionConfigRepository.count() == 0) {
            crearDeduccion("INSS Laboral", "7.000", "Aporte del trabajador a la seguridad social", "PORCENTAJE", false);
            crearDeduccion("IR (Impuesto sobre la Renta)", "2.000", "Retención de impuesto sobre la renta", "IR", false);
            crearDeduccion("Préstamo", "5.000", "Descuento de préstamo de empresa", "PORCENTAJE", false);
            log.info("Configuración de deducciones creada");
        } else {
            correccionInssDuplicado();
        }
    }

    private void correccionInssDuplicado() {
        for (DeduccionConfig d : deduccionConfigRepository.findByNombreContainingIgnoreCase("INSS Laboral")) {
            if (Boolean.TRUE.equals(d.getActiva())) {
                d.setActiva(false);
                deduccionConfigRepository.save(d);
                log.warn("Se desactivó la deducción 'INSS Laboral' para evitar deducción duplicada con el cálculo nativo");
            }
        }
    }

    private void crearEmpleado(String codigo, String nombre, String apellido, String cedula,
                               String cargo, String departamento, String tarifa, LocalDate fecha) {
        Empleado e = new Empleado();
        e.setCodigo(codigo);
        e.setNombre(nombre);
        e.setApellido(apellido);
        e.setCedula(cedula);
        e.setCargo(cargo);
        e.setDepartamento(departamento);
        e.setTarifaHora(new BigDecimal(tarifa));
        e.setHorasJornada(8);
        e.setFechaContratacion(fecha);
        e.setActivo(true);
        e.setTelefono("8888-0000");
        e.setDireccion("Managua, Nicaragua");
        empleadoRepository.save(e);
    }

    private void crearDeduccion(String nombre, String porcentaje, String descripcion, String tipoCalculo, boolean activa) {
        DeduccionConfig d = new DeduccionConfig();
        d.setNombre(nombre);
        d.setPorcentaje(new BigDecimal(porcentaje));
        d.setDescripcion(descripcion);
        d.setTipoCalculo(tipoCalculo);
        d.setActiva(activa);
        deduccionConfigRepository.save(d);
    }
}
