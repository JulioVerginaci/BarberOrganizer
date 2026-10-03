package com.barberorganizer.barberorganizer.Core.Network;

import androidx.annotation.NonNull;

import com.barberorganizer.barberorganizer.Core.Config.SupabaseConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ApiKeyInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        Request requisicao = chain.request()
                .newBuilder()
                .header("apikey", SupabaseConfig.API_KEY)
                .build();

        return chain.proceed(requisicao);
    }
}