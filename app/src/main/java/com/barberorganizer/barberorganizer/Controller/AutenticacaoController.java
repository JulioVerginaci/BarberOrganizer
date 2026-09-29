package com.barberorganizer.barberorganizer.Controller;

import android.util.Patterns;

public class AutenticacaoController {

    // Retorna a mensagem de erro, ou null se todos os dados estiverem válidos.
    public String validarCadastro(String nome, String telefone, String email,
                                  String senha, String confirmarSenha) {

        if (nome == null || nome.trim().isEmpty()) {
            return "Informe o nome completo.";
        }
        if (telefone == null || telefone.trim().isEmpty()) {
            return "Informe o telefone.";
        }
        if (email == null || email.trim().isEmpty()) {
            return "Informe o e-mail.";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Informe um e-mail válido.";
        }
        if (senha == null || senha.isEmpty()) {
            return "Informe a senha.";
        }
        if (senha.length() < 6) {
            return "A senha deve ter pelo menos 6 caracteres.";
        }
        if (confirmarSenha == null || confirmarSenha.isEmpty()) {
            return "Confirme a senha.";
        }
        if (!senha.equals(confirmarSenha)) {
            return "As senhas não conferem.";
        }

        return null;
    }

    // Retorna a mensagem de erro, ou null se os dados estiverem válidos.
    public String validarLogin(String email, String senha) {

        if (email == null || email.trim().isEmpty()) {
            return "Informe o e-mail.";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Informe um e-mail válido.";
        }
        if (senha == null || senha.trim().isEmpty()) {
            return "Informe a senha.";
        }

        return null;
    }
}