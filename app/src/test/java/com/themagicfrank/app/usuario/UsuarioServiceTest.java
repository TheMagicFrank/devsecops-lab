package com.themagicfrank.app.usuario;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UsuarioServiceTest {

    @Autowired
    private UsuarioService service;

    @Test
    void crearYBuscarUsuario() {
        Usuario creado = service.crear(new Usuario("Ana", "ana@example.com"));

        assertThat(creado.getId()).isNotNull();
        assertThat(service.buscarPorId(creado.getId()))
                .isPresent()
                .get()
                .extracting(Usuario::getNombre)
                .isEqualTo("Ana");
    }

    @Test
    void listarDevuelveLosUsuariosCreados() {
        service.crear(new Usuario("Ana", "ana@example.com"));
        service.crear(new Usuario("Luis", "luis@example.com"));

        assertThat(service.listar()).hasSize(2);
    }

    @Test
    void eliminarUsuarioExistente() {
        Usuario creado = service.crear(new Usuario("Ana", "ana@example.com"));

        assertThat(service.eliminar(creado.getId())).isTrue();
        assertThat(service.buscarPorId(creado.getId())).isEmpty();
    }

    @Test
    void eliminarUsuarioInexistenteDevuelveFalse() {
        assertThat(service.eliminar(9999L)).isFalse();
    }
}
