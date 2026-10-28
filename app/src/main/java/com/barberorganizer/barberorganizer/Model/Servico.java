package com.barberorganizer.barberorganizer.Model;

import com.google.gson.annotations.SerializedName;

public class Servico {
    @SerializedName("servico_id") private String id;
    @SerializedName("nome_servico") private String nome;
    @SerializedName("preco") private double preco;
    @SerializedName("duracao") private int duracao;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }
    public int getDuracao() { return duracao > 0 ? duracao : 30; }
    public void setDuracao(int duracao) { this.duracao = duracao; }

    @Override public String toString() {
        return (nome == null ? "Serviço" : nome) + " • " + String.format(java.util.Locale.forLanguageTag("pt-BR"), "R$ %.2f", preco).replace('.', ',');
    }
}