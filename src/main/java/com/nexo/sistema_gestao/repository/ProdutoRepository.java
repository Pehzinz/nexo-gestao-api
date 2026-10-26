package com.nexo.sistema_gestao.repository;

import com.nexo.sistema_gestao.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, String> {

    // Retorna apenas os produtos ativos para o Caixa e a Tabela de Estoque
    List<Produto> findAllByAtivoTrue();
    
    // Busca todos os produtos ativos que pertencem a uma categoria específica
    List<Produto> findByCategoriaIdAndAtivoTrue(Long categoriaId);

    // Busca produto por ID somente se estiver ativo
    Optional<Produto> findByIdAndAtivoTrue(String id);
}