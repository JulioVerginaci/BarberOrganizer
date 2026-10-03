package com.barberorganizer.barberorganizer.Service;

import android.content.Context;
import android.util.Patterns;

import com.barberorganizer.barberorganizer.Core.Network.RepassadorCallback;
import com.barberorganizer.barberorganizer.Core.Network.ResultadoCallback;
import com.barberorganizer.barberorganizer.Core.Session.SessaoManager;
import com.barberorganizer.barberorganizer.Model.CadastroRequest;
import com.barberorganizer.barberorganizer.Model.LoginRequest;
import com.barberorganizer.barberorganizer.Model.LoginResponse;
import com.barberorganizer.barberorganizer.Model.Resultado;
import com.barberorganizer.barberorganizer.Repository.UsuarioRepository;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final SessaoManager sessaoManager;

    public UsuarioService(Context context) {
        usuarioRepository = new UsuarioRepository(context);
        sessaoManager = new SessaoManager(
                context.getApplicationContext()
        );
    }

    public Resultado login(
            LoginRequest request,
            ResultadoCallback<Void> callback
    ) {

        Resultado validacao = validarCredenciais(
                request.getEmail(),
                request.getPassword()
        );

        if (!validacao.isSucesso()) {
            return validacao;
        }

        usuarioRepository.login(
                request,
                new RepassadorCallback<LoginResponse, Void>(callback) {

                    @Override
                    public void onSucesso(LoginResponse resposta) {

                        if (salvarSessaoSeHouver(resposta)) {
                            destino.onSucesso(null);
                        } else {
                            destino.onErro(
                                    "Não foi possível iniciar a sessão. Tente novamente."
                            );
                        }
                    }
                }
        );

        return Resultado.ok();
    }

    public Resultado cadastrar(
            CadastroRequest request,
            ResultadoCallback<Boolean> callback
    ) {

        Resultado validacao = validarCadastro(
                request.getNome(),
                request.getEmail(),
                request.getPassword()
        );

        if (!validacao.isSucesso()) {
            return validacao;
        }

        usuarioRepository.cadastrar(
                request,
                new RepassadorCallback<LoginResponse, Boolean>(callback) {

                    @Override
                    public void onSucesso(LoginResponse resposta) {

                        boolean jaLogado =
                                salvarSessaoSeHouver(resposta);

                        destino.onSucesso(jaLogado);
                    }
                }
        );

        return Resultado.ok();
    }

    private Resultado validarCredenciais(
            String email,
            String senha
    ) {

        if (email == null || email.trim().isEmpty()) {
            return Resultado.erro(
                    Resultado.Campo.EMAIL,
                    "Informe o e-mail."
            );
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Resultado.erro(
                    Resultado.Campo.EMAIL,
                    "Informe um e-mail válido."
            );
        }

        if (senha == null || senha.trim().isEmpty()) {
            return Resultado.erro(
                    Resultado.Campo.SENHA,
                    "Informe a senha."
            );
        }

        return Resultado.ok();
    }

    private Resultado validarCadastro(
            String nome,
            String email,
            String senha
    ) {

        if (nome == null || nome.trim().isEmpty()) {
            return Resultado.erro(
                    Resultado.Campo.NOME,
                    "Informe o nome completo."
            );
        }

        if (email == null || email.trim().isEmpty()) {
            return Resultado.erro(
                    Resultado.Campo.EMAIL,
                    "Informe o e-mail."
            );
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Resultado.erro(
                    Resultado.Campo.EMAIL,
                    "Informe um e-mail válido."
            );
        }

        if (senha == null || senha.isEmpty()) {
            return Resultado.erro(
                    Resultado.Campo.SENHA,
                    "Informe a senha."
            );
        }

        if (senha.length() < 6) {
            return Resultado.erro(
                    Resultado.Campo.SENHA,
                    "A senha deve ter pelo menos 6 caracteres."
            );
        }

        return Resultado.ok();
    }

    private boolean salvarSessaoSeHouver(LoginResponse resposta) {

        if (resposta == null
                || resposta.getAccessToken() == null
                || resposta.getUser() == null) {
            return false;
        }

        sessaoManager.salvarSessao(
                resposta.getAccessToken(),
                resposta.getRefreshToken(),
                resposta.getUser().getId()
        );

        return true;
    }
}