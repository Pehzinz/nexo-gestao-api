package com.nexo.sistema_gestao.model;

import jakarta.persistence.*;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    private String id; // Código de barras (10 dígitos)

    @Column(nullable = false)
    private String nome;

    private String marca;

    @Column(nullable = false)
    private Double preco;

    @Column(nullable = false)
    private Integer estoque;

    @Column(nullable = false)
    private boolean ativo = true; // Campo para o Soft Delete

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // Adicione este atributo junto com o nome, marca, preco, etc.
    @Column(length = 500)
    private String imagemUrl;

    // ... seus outros atributos (nome, marca, preco, imagemUrl)
    @Column(nullable = true)
    private Double precoPromocional;
    
    @Column(nullable = true)
    private java.time.LocalDate dataFimPromocao; // Guarda o último dia da validade
    
    @Column(nullable = true)
    private java.time.LocalDate dataInicioPromocao;





    public Produto() {}

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public Double getPreco() { return preco; }
    public void setPreco(Double preco) { this.preco = preco; }
    public Integer getEstoque() { return estoque; }
    public void setEstoque(Integer estoque) { this.estoque = estoque; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public String getImagemUrl(){return this.imagemUrl;}
    public void setImagemUrl(String imagemUrl){this.imagemUrl = imagemUrl;}
    public Double getPrecoPromocional() {return precoPromocional;}
    public void setPrecoPromocional(Double precoPromocional) {this.precoPromocional = precoPromocional;}
    public java.time.LocalDate getDataInicioPromocao() {return dataInicioPromocao;}
    public void setDataInicioPromocao(java.time.LocalDate dataInicioPromocao) {this.dataInicioPromocao = dataInicioPromocao;}
    public java.time.LocalDate getDataFimPromocao() {return dataFimPromocao;}
    public void setDataFimPromocao(java.time.LocalDate dataFimPromocao) {this.dataFimPromocao = dataFimPromocao;}
}