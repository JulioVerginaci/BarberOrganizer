package com.barberorganizer.barberorganizer.Repository;

import android.content.Context;

import com.barberorganizer.barberorganizer.Core.Network.AuthApi;
import com.barberorganizer.barberorganizer.Core.Network.RespostaCallback;
import com.barberorganizer.barberorganizer.Core.Network.ResultadoCallback;
import com.barberorganizer.barberorganizer.Core.Network.RetrofitClient;
import com.barberorganizer.barberorganizer.Model.CadastroRequest;
import com.barberorganizer.barberorganizer.Model.LoginRequest;
import com.barberorganizer.barberorganizer.Model.LoginResponse;
import com.barberorganizer.barberorganizer.Model.SupabaseUser;

public class UsuarioRepository {

    private final AuthApi authApi;

    public UsuarioRepository(Context context) {
        authApi = RetrofitClient
                .getInstance(context)
                .create(AuthApi.class);
    }

    public void login(
            LoginRequest request,
            ResultadoCallback<LoginResponse> callback
    ) {
        authApi
                .login("password", request)
                .enqueue(new RespostaCallback<>(callback));
    }

    public void cadastrar(
            CadastroRequest request,
            ResultadoCallback<LoginResponse> callback
    ) {
        authApi
                .cadastrar(request)
                .enqueue(new RespostaCallback<>(callback));
    }

    public void obterUsuario(
            ResultadoCallback<SupabaseUser> callback
    ) {
        authApi
                .obterUsuario()
                .enqueue(new RespostaCallback<>(callback));
    }

    public void logout(
            ResultadoCallback<Void> callback
    ) {
        authApi
                .logout()
                .enqueue(new RespostaCallback<>(callback));
    }
}