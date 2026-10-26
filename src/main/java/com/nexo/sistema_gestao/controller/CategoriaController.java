package com.nexo.sistema_gestao.controller;

import com.nexo.sistema_gestao.model.Categoria;
import com.nexo.sistema_gestao.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private com.nexo.sistema_gestao.repository.ProdutoRepository produtoRepository;

    // Listar apenas categorias ATIVAS
    @GetMapping
    public ResponseEntity<List<Categoria>> listarTodas() {
        return ResponseEntity.ok(categoriaRepository.findAllByAtivoTrue());
    }

    // Criar com Inteligência de Reativação pelo Nome
    @PostMapping
    public ResponseEntity<?> criarCategoria(@RequestBody Categoria novaCategoria) {
        String nomeFormatado = novaCategoria.getNome().trim();
        Optional<Categoria> existenteOpt = categoriaRepository.findByNomeIgnoreCase(nomeFormatado);

        if (existenteOpt.isPresent()) {
            Categoria categoriaExistente = existenteOpt.get();
            if (!categoriaExistente.isAtivo()) {
                // Reativa a categoria inativa!
                categoriaExistente.setAtivo(true);
                categoriaRepository.save(categoriaExistente);
                return ResponseEntity.ok(categoriaExistente);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"erro\": \"Categoria já cadastrada e ativa.\"}");
            }
        }

        novaCategoria.setNome(nomeFormatado);
        novaCategoria.setAtivo(true);
        Categoria salva = categoriaRepository.save(novaCategoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }

    // Soft Delete para Categoria
    @DeleteMapping("/{id}")
    public ResponseEntity<?> inativarCategoria(@PathVariable Long id) {
        Optional<Categoria> catOpt = categoriaRepository.findById(id);
        if (catOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Categoria categoria = catOpt.get();
        categoria.setAtivo(false); // Inativa em vez de deletar
        categoriaRepository.save(categoria);

        return ResponseEntity.noContent().build();
    }
    

@DeleteMapping("/nome/{nome}")
    public ResponseEntity<?> inativarCategoriaPorNome(@PathVariable String nome) {
        Optional<Categoria> catOpt = categoriaRepository.findByNomeIgnoreCase(nome.trim());
        
        if (catOpt.isEmpty() || !catOpt.get().isAtivo()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{\"erro\": \"Categoria não encontrada ou já excluída.\"}");
        }

        Categoria categoria = catOpt.get();
        List<com.nexo.sistema_gestao.model.Produto> produtos = produtoRepository.findByCategoriaIdAndAtivoTrue(categoria.getId());

        // 1. Trava: Verifica se algum produto da categoria ainda possui estoque
        for (com.nexo.sistema_gestao.model.Produto p : produtos) {
            if (p.getEstoque() > 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"erro\": \"Ação negada: O produto '" + p.getNome() + "' pertencente a esta categoria ainda possui " + p.getEstoque() + " unidades em estoque.\"}");
            }
        }

        // 2. EXCLUSÃO EM CASCATA: Inativa a categoria...
        categoria.setAtivo(false);
        categoriaRepository.save(categoria);

        // ... e inativa todos os produtos atrelados a ela de uma vez só!
        for (com.nexo.sistema_gestao.model.Produto p : produtos) {
            p.setAtivo(false);
            produtoRepository.save(p);
        }

        return ResponseEntity.noContent().build();
    }
}