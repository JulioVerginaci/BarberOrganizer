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

    public Resultado login(
            String email,
            String senha,
            ResultadoCallback<Void> callback
    ) {
        return usuarioService.login(
                new LoginRequest(
                        limpar(email),
                        senha
                ),
                callback
        );
    }

    public Resultado cadastrar(
            String nome,
            String email,
            String senha,
            ResultadoCallback<Boolean> callback
    ) {
        return usuarioService.cadastrar(
                new CadastroRequest(
                        limpar(email),
                        senha,
                        limpar(nome)
                ),
                callback
        );
    }

    public boolean estaLogado() {
        return sessaoManager.estaLogado();
    }

    public String obterUsuarioId() {
        return sessaoManager.getUsuarioId();
    }

    public void logout() {
        sessaoManager.limparSessao();
    }

    private String limpar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}