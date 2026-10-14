package com.themagicfrank.app.usuario;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final EntityManager entityManager;

    public UsuarioService(UsuarioRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    public List<Usuario> listar() {
        return repository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Usuario crear(Usuario usuario) {
        return repository.save(usuario);
    }

    public boolean eliminar(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    // VULNERABLE (DSL-11): construye la consulta concatenando la entrada del usuario.
    // Permite inyeccion SQL. Se corrige en el commit siguiente con consulta parametrizada.
    @SuppressWarnings("unchecked")
    public List<Usuario> buscarPorNombre(String nombre) {
        String sql = "SELECT * FROM usuario WHERE nombre = '" + nombre + "'";
        Query query = entityManager.createNativeQuery(sql, Usuario.class);
        return query.getResultList();
    }
}
