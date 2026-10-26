package com.nexo.sistema_gestao.repository;

import com.nexo.sistema_gestao.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    
    // Retorna apenas categorias ativas para listar no Front-end
    List<Categoria> findAllByAtivoTrue();

    // Usado para busca de reativação pelo Nome (ignorando maiúsculas/minúsculas)
    Optional<Categoria> findByNomeIgnoreCase(String nome);
}