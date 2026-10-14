package com.themagicfrank.app.usuario;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test de regresión de seguridad para DSL-14 (mass assignment).
 */
@SpringBootTest
@Transactional
class UsuarioControllerTest {

    @Autowired
    private UsuarioController controller;

    @Autowired
    private UsuarioService service;

    @Test
    void crearNoSobrescribeUsuariosExistentes() {
        Usuario ana = service.crear(new Usuario("Ana", "ana@example.com"));

        Usuario nuevo = controller.crear(new UsuarioRequest("Atacante", "atacante@example.com")).getBody();

        assertThat(nuevo).isNotNull();
        assertThat(nuevo.getId()).isNotEqualTo(ana.getId());
        assertThat(service.buscarPorId(ana.getId()))
                .isPresent()
                .get()
                .extracting(Usuario::getNombre)
                .isEqualTo("Ana");
    }
}
