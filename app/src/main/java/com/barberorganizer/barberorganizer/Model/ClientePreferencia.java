
package com.barberorganizer.barberorganizer.Model;

import com.google.gson.annotations.SerializedName;

public class ClientePreferencia {
    @SerializedName("usuario_id")
    private String usuarioId;

    @SerializedName("corte_favorito_id")
    private String corteFavoritoId;

    public ClientePreferencia() { }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getCorteFavoritoId() {
        return corteFavoritoId;
    }

    public void setCorteFavoritoId(String corteFavoritoId) {
        this.corteFavoritoId = corteFavoritoId;
    }
}
