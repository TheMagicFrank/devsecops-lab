package com.themagicfrank.app.usuario;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test de regresion de seguridad para DSL-11 (inyeccion SQL).
 * Reproduce el payload x' OR '1'='1 y verifica que ya no devuelve todos los usuarios.
 */
@SpringBootTest
@Transactional
class UsuarioSqlInjectionTest {

    @Autowired
    private UsuarioService service;

    @Test
    void laBusquedaPorNombreEsInmuneAInyeccionSql() {
        service.crear(new Usuario("Ana", "ana@example.com"));
        service.crear(new Usuario("Luis", "luis@example.com"));

        // Payload clasico de inyeccion SQL
        var resultado = service.buscarPorNombre("x' OR '1'='1");

        // Con la consulta parametrizada, el payload se trata como un nombre literal:
        // no existe ningun usuario asi, asi que el resultado debe estar vacio
        assertThat(resultado).isEmpty();
    }

    @Test
    void laBusquedaPorNombreDevuelveLaCoincidenciaExacta() {
        service.crear(new Usuario("Ana", "ana@example.com"));
        service.crear(new Usuario("Luis", "luis@example.com"));

        var resultado = service.buscarPorNombre("Ana");

        assertThat(resultado)
                .hasSize(1)
                .first()
                .extracting(Usuario::getNombre)
                .isEqualTo("Ana");
    }
}
