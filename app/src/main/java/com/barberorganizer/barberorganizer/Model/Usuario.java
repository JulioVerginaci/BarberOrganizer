package com.barberorganizer.barberorganizer.Model;

public class Usuario {
    private String id;
    private String nome;
    private String email;
    private String telefone;

    public Usuario(String id, String nome, String email, String telefone) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getTelefone() { return telefone; }
}