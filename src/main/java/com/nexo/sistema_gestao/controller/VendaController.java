package com.nexo.sistema_gestao.controller;

import com.nexo.sistema_gestao.dto.ItemVendaRequestDTO;
import com.nexo.sistema_gestao.dto.VendaRequestDTO;
import com.nexo.sistema_gestao.model.ItemVenda;
import com.nexo.sistema_gestao.model.Produto;
import com.nexo.sistema_gestao.model.Usuario;
import com.nexo.sistema_gestao.model.Venda;
import com.nexo.sistema_gestao.repository.VendaRepository;
import com.nexo.sistema_gestao.service.VendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/vendas")
@CrossOrigin(origins = "*")
public class VendaController {

    @Autowired
    private VendaService vendaService;

    @Autowired
    private VendaRepository vendaRepository;

    // 1. ROTA PARA O DASHBOARD LER AS VENDAS
    @GetMapping
    public ResponseEntity<List<Venda>> listarVendas() {
        List<Venda> vendas = vendaRepository.findAll();
        return ResponseEntity.ok(vendas);
    }

    // 2. ROTA DE REGISTRAR VENDA CORRIGIDA (Sem dupla dedução de estoque)
    @PostMapping
    public ResponseEntity<?> realizarVenda(@RequestBody VendaRequestDTO request) {
        try {
            Venda venda = new Venda();
            Usuario usuario = new Usuario();
            usuario.setId(request.usuarioId());
            
            venda.setUsuario(usuario);
            venda.setFormaPagamento(request.formaPagamento());
            venda.setTotal(request.total());

            List<ItemVenda> itens = new ArrayList<>();
            for (ItemVendaRequestDTO itemDTO : request.itens()) {
                ItemVenda item = new ItemVenda();
                Produto produto = new Produto();
                produto.setId(itemDTO.produtoId()); 
                
                item.setProduto(produto);
                item.setQuantidade(itemDTO.quantidade());
                item.setPrecoUnitario(itemDTO.precoUnitario());
                item.setSubtotal(itemDTO.subtotal());
                
                itens.add(item);
            }

            // O VendaService tem o @Transactional. Ele salvará a venda e baixará o estoque UMA ÚNICA VEZ.
            Venda vendaRealizada = vendaService.finalizarVenda(venda, itens);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(vendaRealizada);

        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"erro\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"erro\": \"Falha interna no servidor.\"}");
        }
    }
}