package com.barberorganizer.barberorganizer.Core.Session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessaoManager {

    private static final String NOME_PREFERENCIAS = "sessao";

    private static final String USUARIO_ID = "usuario_id";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";

    private final SharedPreferences preferences;

    public SessaoManager(Context context) {
        preferences = context.getSharedPreferences(
                NOME_PREFERENCIAS,
                Context.MODE_PRIVATE
        );
    }

    public void salvarSessao(
            String accessToken,
            String refreshToken,
            String usuarioId
    ) {
        preferences.edit()
                .putString(ACCESS_TOKEN, accessToken)
                .putString(REFRESH_TOKEN, refreshToken)
                .putString(USUARIO_ID, usuarioId)
                .apply();
    }

    public String getAccessToken() {
        return preferences.getString(ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return preferences.getString(REFRESH_TOKEN, null);
    }

    public String getUsuarioId() {
        return preferences.getString(USUARIO_ID, null);
    }

    public boolean estaLogado() {
        return getAccessToken() != null;
    }

    public void limparSessao() {
        preferences.edit()
                .clear()
                .apply();
    }
}