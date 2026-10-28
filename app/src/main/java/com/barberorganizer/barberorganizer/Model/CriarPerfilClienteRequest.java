package com.barberorganizer.barberorganizer.Model;

import com.google.gson.annotations.SerializedName;

public class CriarPerfilClienteRequest {

    @SerializedName("p_usuario_id")
    private final String usuarioId;

    @SerializedName("p_nome")
    private final String nome;

    @SerializedName("p_email")
    private final String email;

    @SerializedName("p_telefone")
    private final String telefone;

    public CriarPerfilClienteRequest(
            String usuarioId,
            String nome,
            String email,
            String telefone
    ) {
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }
}