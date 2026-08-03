package com.fabrica.pagos.config;

import com.fabrica.pagos.model.Empresa;
import com.fabrica.pagos.service.EmpresaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class MenuAdvice {

    private final EmpresaService empresaService;

    public MenuAdvice(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @ModelAttribute("requestUri")
    public String requestUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("empresa")
    public Empresa empresa() {
        return empresaService.obtener();
    }
}
