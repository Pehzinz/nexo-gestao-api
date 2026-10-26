//Nesta classe, introduziremos um dos conceitos mais importantes para a segurança e confiabilidade de sistemas financeiros e de vendas: o Controle de Transação (@Transactional)
package com.nexo.sistema_gestao.service;
import com.nexo.sistema_gestao.model.Venda;
import com.nexo.sistema_gestao.model.ItemVenda;
import com.nexo.sistema_gestao.repository.ItemVendaRepository;
import com.nexo.sistema_gestao.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service 
public class VendaService {
    //Atributos do Repositorio e Dependencias do Objeto Produto
    @Autowired 
    private VendaRepository vendaRepo;
    
    @Autowired 
    private ItemVendaRepository itemRepo;

    @Autowired 
    private ProdutoService produtoService;


    //Registra uma nova venda vinda do Caixa de Autoatendimento:
    //@Transactional blinda a operação -> Ou salva TUDO(venda + item + baixaEstoque) ou não salva NADA;
    @Transactional 
    public Venda finalizarVenda (Venda venda, List<ItemVenda> itens){
        try{
            //Salva venda Principal para gerar o ID no banco de dados;
            Venda vendaSalva = vendaRepo.save(venda);

            //Processa cada produto adicionado no carrinho de vendas;
            for(ItemVenda item : itens){
                //vincula o Item para a venda recem-criada;
                item.setVenda(vendaSalva);

                // Se não houver estoque, isso vai gerar um erro e o @Transactional vai cancelar a venda.
                produtoService.baixarEstoque(item.getProduto().getId(), item.getQuantidade());

                //Salva o item da venda no banco de dados;
                itemRepo.save(item);
            } 
            return vendaSalva;


        } catch (IllegalArgumentException | IllegalStateException e) {
            // Captura erros de negócio (ex: sem estoque) gerados no ProdutoService
            System.err.println("VENDA RECUSADA POR REGRA DE NEGÓCIO: " + e.getMessage());
            throw e; 
        } catch (Exception e) {
            // Captura falhas críticas do sistema ou banco de dados
            System.err.println("ERRO CRÍTICO AO FINALIZAR VENDA: " + e.getMessage());
            throw new RuntimeException("Falha inesperada ao processar a venda. Operação cancelada de forma segura.", e);
        }
    }
}











