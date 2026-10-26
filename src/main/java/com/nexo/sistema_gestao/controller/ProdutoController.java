package com.nexo.sistema_gestao.controller;


import com.nexo.sistema_gestao.model.Produto;
import com.nexo.sistema_gestao.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
public class ProdutoController {

    @Autowired
    private ProdutoRepository produtoRepository;

    // Listar apenas produtos ATIVOS
    @GetMapping
    public ResponseEntity<List<Produto>> listarTodos() {
        return ResponseEntity.ok(produtoRepository.findAllByAtivoTrue());
    }

    // Buscar por ID apenas se estiver ATIVO
    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable String id) {
        return produtoRepository.findByIdAndAtivoTrue(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Criar com Inteligência de Reativação pelo Código de Barras (ID)
    @PostMapping
    public ResponseEntity<?> salvarProduto(@RequestBody Produto produto) {
        Optional<Produto> existenteOpt = produtoRepository.findById(produto.getId());

        if (existenteOpt.isPresent()) {
            Produto existente = existenteOpt.get();
            if (!existente.isAtivo()) {
                // Reativa o produto e atualiza suas informações!
                existente.setNome(produto.getNome());
                existente.setMarca(produto.getMarca());
                existente.setPreco(produto.getPreco());
                existente.setEstoque(produto.getEstoque());
                existente.setCategoria(produto.getCategoria());
                
                // === A CORREÇÃO ENTRA AQUI: Puxar a promoção ao reativar ===
                existente.setPrecoPromocional(produto.getPrecoPromocional());
                existente.setDataInicioPromocao(produto.getDataInicioPromocao());
                existente.setDataFimPromocao(produto.getDataFimPromocao());
                // ==========================================================

                existente.setAtivo(true); // REATIVADO!

                produtoRepository.save(existente);
                return ResponseEntity.ok(existente);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"erro\": \"Código de barras já cadastrado em um produto ativo.\"}");
            }
        }

        // Se o código for totalmente novo, ele cai aqui (e já salva a promoção automaticamente)
        produto.setAtivo(true);
        Produto salvo = produtoRepository.save(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // Soft Delete com trava de estoque zero
    @DeleteMapping("/{id}")
    public ResponseEntity<?> inativarProduto(@PathVariable String id) {
        Optional<Produto> prodOpt = produtoRepository.findByIdAndAtivoTrue(id);

        if (prodOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Produto produto = prodOpt.get();

        // Trava do Estoque Zero
        if (produto.getEstoque() > 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("{\"erro\": \"Ação negada: Só é possível inativar produtos com estoque zerado.\"}");
        }

        // Realiza o Soft Delete
        produto.setAtivo(false);
        produtoRepository.save(produto);

        return ResponseEntity.noContent().build();
    }
    
    // Endpoint para Adicionar / Repor Estoque de um produto existente
    @PutMapping("/{id}/adicionar-estoque")
    public ResponseEntity<?> adicionarEstoque(@PathVariable String id, @RequestBody java.util.Map<String, Integer> payload) {
        // 1. Busca o produto garantindo que ele existe e está ativo
        Optional<Produto> prodOpt = produtoRepository.findByIdAndAtivoTrue(id);

        if (prodOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"erro\": \"Produto não encontrado no catálogo de produtos ativos.\"}");
        }

        Integer qtdAdicional = payload.get("quantidade");
        if (qtdAdicional == null || qtdAdicional <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"erro\": \"A quantidade a ser adicionada deve ser maior que zero.\"}");
        }

        // 2. Incrementa o estoque atual com a nova quantidade
        Produto produto = prodOpt.get();
        produto.setEstoque(produto.getEstoque() + qtdAdicional);
        Produto atualizado = produtoRepository.save(produto);

        return ResponseEntity.ok(atualizado);
    }

// ROTA PARA ATUALIZAR UM PRODUTO (FOTO E PROMOÇÃO)
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarProduto(@PathVariable String id, @RequestBody Produto produtoAtualizado) {
        // A VALIDAÇÃO DE SEGURANÇA: Busca no banco primeiro!
        return produtoRepository.findById(id).map(produto -> {
            produto.setNome(produtoAtualizado.getNome());
            produto.setMarca(produtoAtualizado.getMarca());
            produto.setPreco(produtoAtualizado.getPreco());
            produto.setCategoria(produtoAtualizado.getCategoria());
            
            
            // Salva a foto se houver
            if (produtoAtualizado.getImagemUrl() != null) {
                produto.setImagemUrl(produtoAtualizado.getImagemUrl());
            }

            // Salva a promoção (pode ser nulo caso a promoção seja encerrada)
            produto.setPrecoPromocional(produtoAtualizado.getPrecoPromocional());
            produto.setDataInicioPromocao(produtoAtualizado.getDataInicioPromocao());
            produto.setDataFimPromocao(produtoAtualizado.getDataFimPromocao());
            
            produtoRepository.save(produto);
            
            produtoRepository.save(produto);
            return ResponseEntity.ok().body("{\"mensagem\": \"Produto atualizado com sucesso!\"}");
            
        }).orElse(ResponseEntity.status(404).body("{\"erro\": \"Produto não encontrado no sistema!\"}"));
    }
}