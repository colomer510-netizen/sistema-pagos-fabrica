package com.fabrica.pagos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CambioPasswordInterceptor cambioPasswordInterceptor;

    public WebConfig(CambioPasswordInterceptor cambioPasswordInterceptor) {
        this.cambioPasswordInterceptor = cambioPasswordInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cambioPasswordInterceptor);
    }
}
