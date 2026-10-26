//Validação do estoque;

package com.nexo.sistema_gestao.service;
import com.nexo.sistema_gestao.model.Produto;
import com.nexo.sistema_gestao.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service 
public class ProdutoService {
    //Atributos da classe;
    @Autowired 
    private ProdutoRepository repo;

    //Metodo para diminuir o estoque após uma venda;
    public void baixarEstoque(String produtoId, int qtdVendida){
        try{
            //Validando se o produto existe no sistema -> Banco de Dados
            Optional<Produto> produtoOpt = repo.findById(produtoId);
            if(produtoOpt.isEmpty()){
                throw new IllegalArgumentException("Erro: Produto com ID " + produtoId + "não encontrado no sistema");
            }

            //Pega todas as informacoes de objeto que o produtoId referencia;
            Produto produto = produtoOpt.get();  

            //Validando se a quantidade soliciata é valida;
            if(qtdVendida <= 0){
                throw new IllegalArgumentException("Erro: Quantidade vendida deve ser maior que zero!");
            }
            
            //validando se há estoque suficiente;
            if(produto.getEstoque() < qtdVendida){
                throw new IllegalStateException("Erro: Estoque insuficiente para o produto: " + produto.getNome() + ". Estoque atual: " + produto.getEstoque());
            }

            //Executando a baixa e salvando no banco;
            int novoEstoque = produto.getEstoque() - qtdVendida;
            produto.setEstoque(novoEstoque);  //Novo tamanho de estoque atribuido ao Objeto Produto;
            repo.save(produto);  //Salvando no Banco de dados;


        //validação de uma API quando o programa for vendido;
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Em uma API real, essas exceções são capturadas pelo Controller e devolvidas 
            // como um erro 400 (Bad Request) com a mensagem exata para o Front-end.
            System.err.println("FALHA NA VALIDAÇÃO DE ESTOQUE: " + e.getMessage());
            throw e; // Repassa o erro para impedir que a venda seja concluída
        } catch (Exception e) {
            // Captura qualquer erro inesperado (ex: queda do banco de dados)
            System.err.println("ERRO CRÍTICO AO ACESSAR O BANCO: " + e.getMessage());
            throw new RuntimeException("Erro interno ao processar o estoque.", e);
        }
    }

}

