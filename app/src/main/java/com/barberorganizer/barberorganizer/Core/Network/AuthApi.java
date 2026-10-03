package com.barberorganizer.barberorganizer.Core.Network;

import com.barberorganizer.barberorganizer.Model.CadastroRequest;
import com.barberorganizer.barberorganizer.Model.LoginRequest;
import com.barberorganizer.barberorganizer.Model.LoginResponse;
import com.barberorganizer.barberorganizer.Model.RefreshTokenRequest;
import com.barberorganizer.barberorganizer.Model.SupabaseUser;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthApi {

    @POST("auth/v1/signup")
    Call<LoginResponse> cadastrar(
            @Body CadastroRequest request
    );

    @POST("auth/v1/token")
    Call<LoginResponse> login(
            @Query("grant_type") String grantType,
            @Body LoginRequest request
    );

    @POST("auth/v1/token")
    Call<LoginResponse> atualizarToken(
            @Query("grant_type") String grantType,
            @Body RefreshTokenRequest request
    );

    @GET("auth/v1/user")
    Call<SupabaseUser> obterUsuario();

    @POST("auth/v1/logout")
    Call<Void> logout();
}