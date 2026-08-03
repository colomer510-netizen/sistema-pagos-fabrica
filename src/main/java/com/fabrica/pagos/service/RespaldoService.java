package com.fabrica.pagos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class RespaldoService {

    private static final DateTimeFormatter MARCA = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final Path directorioRespaldo;
    private final DataSource dataSource;

    public RespaldoService(@Value("${app.respaldo.directorio:./data/backups}") String directorio,
                           DataSource dataSource) {
        this.directorioRespaldo = Paths.get(directorio).toAbsolutePath().normalize();
        this.dataSource = dataSource;
    }

    public Path getDirectorioRespaldo() {
        return directorioRespaldo;
    }

    public String crearRespaldo() throws Exception {
        Files.createDirectories(directorioRespaldo);
        String nombre = "fabrica-" + LocalDateTime.now().format(MARCA) + ".db";
        Path destino = directorioRespaldo.resolve(nombre);
        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
            st.execute("VACUUM INTO '" + destino.toString().replace("'", "''") + "'");
        }
        return nombre;
    }

    public List<Path> listarRespaldo() {
        if (!Files.isDirectory(directorioRespaldo)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(directorioRespaldo)) {
            return stream.filter(p -> p.toString().endsWith(".db"))
                    .sorted(Comparator.reverseOrder())
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    public byte[] descargar(String nombre) throws IOException {
        Path archivo = directorioRespaldo.resolve(nombre).normalize();
        if (!archivo.startsWith(directorioRespaldo) || !Files.exists(archivo)) {
            throw new IllegalArgumentException("Respaldo no encontrado");
        }
        return Files.readAllBytes(archivo);
    }

    public void eliminar(String nombre) throws IOException {
        Path archivo = directorioRespaldo.resolve(nombre).normalize();
        if (!archivo.startsWith(directorioRespaldo)) {
            throw new IllegalArgumentException("Respaldo inválido");
        }
        Files.deleteIfExists(archivo);
    }
}
