package com.barberorganizer.barberorganizer.Model;

import com.google.gson.annotations.SerializedName;

/** Tipo de corte disponibilizado pela tabela public.cortes. */
public class Corte {
    @SerializedName("corte_favorito_id")
    private String id;
    @SerializedName("nome_corte")
    private String nome;

    public Corte() { }
    public Corte(String id, String nome) { this.id = id; this.nome = nome; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    @Override public String toString() { return nome == null ? "Corte" : nome; }
}