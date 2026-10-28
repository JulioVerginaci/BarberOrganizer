package com.barberorganizer.barberorganizer.Model;

import com.google.gson.annotations.SerializedName;

public class CriarPerfilAdminRequest {

    @SerializedName("p_usuario_id")
    private final String usuarioId;

    @SerializedName("p_nome")
    private final String nome;

    @SerializedName("p_email")
    private final String email;

    @SerializedName("p_telefone")
    private final String telefone;

    @SerializedName("p_perfil")
    private final String perfil;

    public CriarPerfilAdminRequest(
            String usuarioId,
            String nome,
            String email,
            String telefone,
            String perfil
    ) {
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.perfil = perfil;
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

    public String getPerfil() {
        return perfil;
    }
}