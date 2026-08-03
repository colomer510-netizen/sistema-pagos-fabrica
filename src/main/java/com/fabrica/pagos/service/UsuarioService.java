package com.fabrica.pagos.service;

import com.fabrica.pagos.model.Usuario;
import com.fabrica.pagos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Value("${app.security.bloqueo-minutos:15}")
    private long bloqueoMinutos;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }
        if (usuario.getFechaBloqueo() != null) {
            LocalDateTime finBloqueo = usuario.getFechaBloqueo().plusMinutes(bloqueoMinutos);
            if (LocalDateTime.now().isBefore(finBloqueo)) {
                throw new org.springframework.security.authentication.LockedException(
                        "Cuenta bloqueada por intentos fallidos");
            }
            usuario.setFechaBloqueo(null);
            usuario.setIntentosFallidos(0);
            usuarioRepository.save(usuario);
        }
        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol())));
    }

    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public boolean requiereCambioPassword(Usuario usuario) {
        if (Boolean.TRUE.equals(usuario.getCambiarPassword())) {
            return true;
        }
        return usuario.getFechaExpiracionPassword() != null
                && LocalDateTime.now().toLocalDate().isAfter(usuario.getFechaExpiracionPassword());
    }
}
