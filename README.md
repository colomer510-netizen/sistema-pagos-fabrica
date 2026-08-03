# Sistema de Gestión de Pagos de Fábrica

Aplicación web para administrar los pagos de empleados de una fábrica: registro de empleados, control de asistencia, cálculo y generación de nóminas, deducciones, préstamos, reportes y respaldos.

Desarrollada con **Spring Boot**, **Thymeleaf**, **Spring Security**, **JPA/Hibernate** y **SQLite**.

## Características

- **Empleados**: alta, edición, eliminación, foto, contacto de emergencia, tipo de sangre y consulta de detalle.
- **Expediente digital**: sube, descarga y elimina documentos por empleado (contratos, cédulas, constancias...).
- **Asistencia**: registro diario por empleado (Presente, Enfermedad, Vacaciones, Falta) y control de horas.
- **Inventario de materiales**: catálogo de materiales con stock, stock mínimo, precio y valor de inventario, más entradas/salidas con historial y alerta de stock bajo.
- **Permisos y vacaciones**: solicitudes de vacaciones/permisos con fechas, cálculo automático de días y aprobación/rechazo.
- **Evaluaciones de desempeño**: puntaje de 0-100 por periodo con calificación automática (EXCELENTE/BUENO/REGULAR/INSUFICIENTE).
- **Liquidaciones**: cálculo automático al terminar la relación laboral (vacaciones pendientes, indemnización por despido) y baja del empleado.
- **Nómina**: generación por periodo con cálculo de horas normales/extras, salario bruto, INSS laboral, deducciones y cuotas de préstamo.
- **Vista previa de nómina**: revisa el cálculo estimado de todo el periodo antes de confirmar y generar.
- **Deducciones configurables**: porcentaje fijo (PORCENTAJE) o impuesto sobre la renta progresivo (IR), con desglose en cada recibo y su PDF.
- **Préstamos**: registro de préstamos por empleado, abonos manuales, liquidación y descuento automático de la cuota en la nómina.
- **Usuarios y permisos**: roles Administrador / Solo lectura, bloqueo por intentos fallidos, forzar cambio de contraseña y expiración.
- **Contabilidad**: cálculo de pago mensual estimado, INSS laboral y patronal, vacaciones, aguinaldo e indemnización.
- **Caja**: registro de ingresos y egresos por categoría, con filtro por mes, saldo del mes y saldo acumulado.
- **Cuentas por pagar**: control de deudas con proveedores, total pendiente, vencimientos y marcado de pago.
- **Presupuestos**: presupuesto mensual por categoría con cruce automático contra los egresos reales de caja (disponible y % de uso).
- **Estado de resultados**: resumen financiero del mes (ingresos - egresos - nómina) y desglose por categoría.
- **Dashboard**: indicadores y gráficos de total pagado por nómina y empleados activos por departamento.
- **Reportes**: exportación a **Excel** y **PDF** de empleados, nóminas, recibos individuales y asistencia por periodo.
- **Respaldos**: copias de seguridad de la base de datos (`VACUUM INTO`) con descarga desde la interfaz.
- **Tutorial interactivo**: guía paso a paso para nuevos usuarios.

## Tecnologías

| Componente | Tecnología |
|---|---|
| Backend | Java 21, Spring Boot 3.3.5 |
| Frontend | Thymeleaf, CSS propio, JavaScript |
| Seguridad | Spring Security (login, roles) |
| Base de datos | SQLite 3.46 (JDBC + dialecto Hibernate) |
| Reportes | Apache POI (Excel), OpenPDF (PDF) |
| Build | Maven |

## Requisitos

- **Java 21** (JDK)
- **Maven 3.9+**

## Ejecución

```bash
# 1. Clonar el repositorio
git clone https://github.com/colomer510-netizen/sistema-pagos-fabrica.git
cd sistema-pagos-fabrica

# 2. Compilar y ejecutar
mvn spring-boot:run
```

La aplicación queda disponible en: **http://localhost:8080**

> En Windows PowerShell, si `mvn` no está en el PATH: `mvn.cmd spring-boot:run`

## Credenciales iniciales

| Rol | Usuario | Contraseña | Permisos |
|---|---|---|---|
| Administrador | `admin` | `admin123` | Crear, editar, eliminar, exportar y gestionar usuarios |
| Solo lectura | `lector` | `lector123` | Consultar y exportar |

> Cambia estas contraseñas al pasar a producción. Puedes crear más usuarios desde la sección **Usuarios**.

## Configuración

El archivo `src/main/resources/application.properties` permite ajustar:

```properties
# Base de datos SQLite
spring.datasource.url=jdbc:sqlite:./data/fabrica.db

# Parámetros contables
app.contabilidad.horas-jornada=8
app.contabilidad.multiplicador-extra=1.5
app.contabilidad.moneda=C$
app.contabilidad.deduccion-inss=7.0
app.contabilidad.deduccion-inss-patronal=22.5

# Parámetros de RRHH
app.rrhh.dias-vacaciones-por-anyo=15
app.rrhh.dias-indemnizacion-por-anyo=30

# Seguridad
app.security.max-intentos=5
app.security.bloqueo-minutos=15
app.security.password-expiracion-dias=0

# Semilla de datos iniciales (usuarios, empleados, deducciones de ejemplo)
app.seed.enabled=true
```

La base de datos se crea automáticamente en `data/fabrica.db`. Los respaldos se guardan en `data/backups`.

## Módulos y rutas principales

| Módulo | Ruta |
|---|---|
| Dashboard | `/dashboard` |
| Empleados | `/empleados` |
| Expediente digital | `/empleados/detalle/{id}` |
| Asistencia | `/asistencia` |
| Inventario de materiales | `/inventario` |
| Permisos y vacaciones | `/permisos` |
| Evaluaciones | `/evaluaciones` |
| Nóminas | `/nomina` |
| Vista previa de nómina | `/nomina/previa` |
| Deducciones | `/deducciones` |
| Préstamos | `/prestamos` |
| Liquidaciones | `/liquidaciones` |
| Contabilidad | `/contabilidad` |
| Caja | `/caja` |
| Cuentas por pagar | `/cuentas-pagar` |
| Presupuestos | `/presupuestos` |
| Estado de resultados | `/finanzas` |
| Reportes | `/reportes` |
| Asistencia (reporte) | `/reportes/asistencia` |
| Usuarios | `/usuarios` |
| Respaldos | `/respaldos` |
| Tutorial | `/tutorial` |

## Estructura del proyecto

```
src/main/java/com/fabrica/pagos/
├── config/          # Seguridad, semilla de datos, consejos de menú
├── controller/      # Controladores MVC (Dashboard, Empleado, Nomina, ...)
├── model/           # Entidades JPA y DTOs
├── repository/      # Repositorios Spring Data JPA
└── service/         # Lógica de negocio (nómina, contabilidad, reportes, respaldos)

src/main/resources/
├── templates/       # Plantillas Thymeleaf
├── static/          # CSS y JavaScript
└── application.properties
```

## Notas

- El INSS laboral se puede ajustar desde la sección **Deducciones** (viene configurada al 7 %); asegúrate de tener solo una deducción de INSS activa para no descontar dos veces.
- Los nuevos campos de la base de datos son nullable en SQLite (el driver no soporta agregar columnas `NOT NULL` sin valor por defecto); la aplicación asigna los valores por defecto en Java.
- Haz respaldos periódicos desde **Respaldos**, especialmente antes de generar nóminas importantes.
