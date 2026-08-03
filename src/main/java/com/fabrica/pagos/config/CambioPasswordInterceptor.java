package com.fabrica.pagos.config;

import com.fabrica.pagos.model.Usuario;
import com.fabrica.pagos.repository.UsuarioRepository;
import com.fabrica.pagos.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CambioPasswordInterceptor implements HandlerInterceptor {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public CambioPasswordInterceptor(UsuarioRepository usuarioRepository, UsuarioService usuarioService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String uri = request.getRequestURI();
        if (uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/img")
                || uri.equals("/cambiar-password") || uri.equals("/login")
                || uri.startsWith("/error")) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Usuario usuario = usuarioRepository.findByUsername(auth.getName()).orElse(null);
            if (usuario != null && usuarioService.requiereCambioPassword(usuario)) {
                response.sendRedirect(request.getContextPath() + "/cambiar-password");
                return false;
            }
        }
        return true;
    }
}
