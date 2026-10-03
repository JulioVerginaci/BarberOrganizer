package com.barberorganizer.barberorganizer.Core.Network;

import android.content.Context;

import androidx.annotation.NonNull;

import com.barberorganizer.barberorganizer.Core.Session.SessaoManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final SessaoManager sessaoManager;

    public AuthInterceptor(Context context) {
        sessaoManager = new SessaoManager(
                context.getApplicationContext()
        );
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        Request original = chain.request();

        if (original.header("Authorization") != null
                || ehRotaPublica(original)) {
            return chain.proceed(original);
        }

        String accessToken = sessaoManager.getAccessToken();

        if (accessToken == null || accessToken.trim().isEmpty()) {
            return chain.proceed(original);
        }

        Request requisicao = original.newBuilder()
                .header(
                        "Authorization",
                        "Bearer " + accessToken
                )
                .build();

        return chain.proceed(requisicao);
    }

    private boolean ehRotaPublica(Request requisicao) {

        String caminho = requisicao.url().encodedPath();

        return caminho.endsWith("/auth/v1/token")
                || caminho.endsWith("/auth/v1/signup");
    }
}