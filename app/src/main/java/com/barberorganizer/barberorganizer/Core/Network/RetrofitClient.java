package com.barberorganizer.barberorganizer.Core.Network;

import android.content.Context;

import com.barberorganizer.barberorganizer.Core.Config.SupabaseConfig;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;

    private RetrofitClient() {
    }

    public static synchronized Retrofit getInstance(Context context) {

        if (retrofit == null) {

            Context appContext = context.getApplicationContext();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new ApiKeyInterceptor())
                    .addInterceptor(new AuthInterceptor(appContext))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(SupabaseConfig.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}