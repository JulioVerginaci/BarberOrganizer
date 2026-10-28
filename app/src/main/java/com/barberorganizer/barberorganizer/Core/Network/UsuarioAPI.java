package com.barberorganizer.barberorganizer.Core.Network;

import com.barberorganizer.barberorganizer.Model.CriarPerfilAdminRequest;
import com.barberorganizer.barberorganizer.Model.CriarPerfilClienteRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UsuarioAPI {

    @GET("rest/v1/rpc/obter_meu_perfil")
    Call<String> obterMeuPerfil();

    @Headers("Prefer: return=minimal")
    @POST("rest/v1/rpc/criar_perfil_cliente")
    Call<Void> criarPerfilCliente(
            @Body CriarPerfilClienteRequest request
    );

    @Headers("Prefer: return=minimal")
    @POST("rest/v1/rpc/admin_criar_usuario_perfil")
    Call<Void> criarPerfilPeloAdmin(
            @Body CriarPerfilAdminRequest request
    );
}