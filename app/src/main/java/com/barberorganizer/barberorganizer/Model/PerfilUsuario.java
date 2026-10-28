package com.barberorganizer.barberorganizer.Model;

import com.google.gson.annotations.SerializedName;

public class PerfilUsuario {

    @SerializedName("usuario_id")
    private String usuarioId;

    public PerfilUsuario() {
    }

    public PerfilUsuario(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
}