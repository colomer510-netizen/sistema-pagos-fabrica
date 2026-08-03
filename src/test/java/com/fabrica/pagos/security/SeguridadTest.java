package com.fabrica.pagos.security;

import com.fabrica.pagos.model.Usuario;
import com.fabrica.pagos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        usuarioRepository.save(usuario("admin", "ADMIN", "admin123"));
        usuarioRepository.save(usuario("lector", "LECTOR", "lector123"));
    }

    private Usuario usuario(String username, String rol, String rawPassword) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombre(username);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRol(rol);
        u.setActivo(true);
        u.setIntentosFallidos(0);
        u.setCambiarPassword(false);
        return u;
    }

    @Test
    void anonimo_dashboard_redirigeAlLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void anonimo_empresa_redirigeAlLogin() throws Exception {
        mockMvc.perform(get("/empresa"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void login_correcto_redirigeAInicio() throws Exception {
        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "admin")
                        .param("password", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void login_passwordIncorrecto_redirigeConError() throws Exception {
        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "admin")
                        .param("password", "incorrecta"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    void admin_puedeVerEmpresa() throws Exception {
        mockMvc.perform(get("/empresa").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void lector_noPuedeVerEmpresa() throws Exception {
        mockMvc.perform(get("/empresa").with(user("lector").roles("LECTOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void post_sinCsrf_devuelve403() throws Exception {
        mockMvc.perform(post("/empleados/guardar"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cincoIntentosFallidos_bloqueaUsuario() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/login").with(csrf())
                            .param("username", "admin")
                            .param("password", "incorrecta"))
                    .andExpect(redirectedUrl("/login?error=true"));
        }
        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "admin")
                        .param("password", "incorrecta"))
                .andExpect(redirectedUrl("/login?error=bloqueado"));
    }
}
