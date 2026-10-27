package com.barberorganizer.barberorganizer.Controller;

import android.content.Context;

import com.barberorganizer.barberorganizer.Core.Network.ResultadoCallback;
import com.barberorganizer.barberorganizer.Core.Session.SessaoManager;
import com.barberorganizer.barberorganizer.Model.CadastroRequest;
import com.barberorganizer.barberorganizer.Model.LoginRequest;
import com.barberorganizer.barberorganizer.Model.Resultado;
import com.barberorganizer.barberorganizer.Service.UsuarioService;

public class UsuarioController {

    private final UsuarioService usuarioService;
    private final SessaoManager sessaoManager;

    public UsuarioController(Context context) {
        usuarioService = new UsuarioService(context);

        sessaoManager = new SessaoManager(
                context.getApplicationContext()
        );
    }

    // O callback retorna o perfil confirmado:
    // CLIENTE, BARBEIRO ou ADMIN.
    public Resultado login(
            String email,
            String senha,
            ResultadoCallback<String> callback
    ) {
        return usuarioService.login(
                new LoginRequest(
                        limpar(email),
                        senha
                ),
                callback
        );
    }

    // Cadastro público: sempre Cliente.
    public Resultado cadastrar(
            String nome,
            String telefone,
            String email,
            String senha,
            ResultadoCallback<Boolean> callback
    ) {
        return usuarioService.cadastrar(
                new CadastroRequest(
                        limpar(email),
                        senha,
                        limpar(nome),
                        limpar(telefone)
                ),
                callback
        );
    }

    // Cadastro exclusivo da área administrativa.
    public Resultado cadastrarPeloAdministrador(
            String nome,
            String telefone,
            String email,
            String senha,
            String perfil,
            ResultadoCallback<Boolean> callback
    ) {
        return usuarioService.cadastrarPeloAdministrador(
                limpar(nome),
                limpar(telefone),
                limpar(email),
                senha,
                perfil,
                callback
        );
    }

    public boolean estaLogado() {
        return sessaoManager.estaLogado();
    }

    public String obterUsuarioId() {
        return sessaoManager.getUsuarioId();
    }

    public String obterPerfilAtual() {
        return sessaoManager.getPerfilUsuario();
    }

    public void logout() {
        sessaoManager.limparSessao();
    }

    private String limpar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}