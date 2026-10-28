package com.barberorganizer.barberorganizer.Model;

import com.google.gson.annotations.SerializedName;

/** Resposta mínima da tabela public.cliente para ler a preferência do usuário logado. */
public class ClientePreferencia {
    @SerializedName("corte_favorito_id")
    private String corteFavoritoId;

    public ClientePreferencia() { }

    public String getCorteFavoritoId() { return corteFavoritoId; }
    public void setCorteFavoritoId(String corteFavoritoId) {
        this.corteFavoritoId = corteFavoritoId;
    }
}