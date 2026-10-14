package com.themagicfrank.app.usuario;

/**
 * Datos que el cliente puede enviar al crear un usuario.
 * Solo nombre y email: el id lo genera siempre el servidor.
 * Evita la vulnerabilidad de mass assignment (DSL-14).
 */
public record UsuarioRequest(String nombre, String email) {

    public Usuario toEntity() {
        return new Usuario(nombre, email);
    }
}
