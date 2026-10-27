package com.barberorganizer.barberorganizer.Core.Network;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Converte respostas HTTP do Retrofit para os callbacks do aplicativo.
 */
public class RespostaCallback<T> implements Callback<T> {

    private static final String TAG = "RespostaCallback";

    private final ResultadoCallback<T> destino;

    public RespostaCallback(ResultadoCallback<T> destino) {
        this.destino = destino;
    }

    @Override
    public void onResponse(
            @NonNull Call<T> call,
            @NonNull Response<T> response
    ) {

        if (!destino.podeReceber()) {
            return;
        }

        String caminho =
                call.request().url().encodedPath();

        Log.d(
                TAG,
                "Resposta HTTP " + response.code()
                        + " para " + caminho
        );

        // Resposta bem-sucedida: encaminha o resultado.
        if (response.isSuccessful()) {

            destino.onSucesso(response.body());
            return;
        }

        /*
         * Um HTTP 401 no cadastro ou login deve ser tratado
         * como erro da própria requisição de autenticação.
         *
         * Para endpoints protegidos, continua sendo possível
         * encaminhar o caso para o tratamento de sessão expirada.
         */
        if (response.code() == 401
                && !ehEndpointPublicoDeAutenticacao(caminho)) {

            destino.onSessaoExpirada();
            return;
        }

        // Qualquer outra resposta de erro é encaminhada à tela.
        destino.onErro(extrairMensagem(response));
    }

    @Override
    public void onFailure(
            @NonNull Call<T> call,
            @NonNull Throwable t
    ) {

        if (call.isCanceled() || !destino.podeReceber()) {
            return;
        }

        Log.e(
                TAG,
                "Falha de comunicação com o servidor.",
                t
        );

        destino.onErro(
                "Não foi possível conectar ao servidor. "
                        + "Verifique sua internet e tente novamente."
        );
    }

    private static boolean ehEndpointPublicoDeAutenticacao(
            String caminho
    ) {

        return caminho.endsWith("/auth/v1/signup")
                || caminho.endsWith("/auth/v1/token");
    }

    private static String extrairMensagem(
            Response<?> response
    ) {

        String padrao =
                "Erro no servidor (código "
                        + response.code() + ").";

        try (ResponseBody corpo = response.errorBody()) {

            if (corpo == null) {
                return padrao;
            }

            String texto = corpo.string();

            Log.w(
                    TAG,
                    "Erro HTTP " + response.code() + ": " + texto
            );

            if (texto.trim().isEmpty()) {
                return padrao;
            }

            JsonElement elemento =
                    JsonParser.parseString(texto);

            if (!elemento.isJsonObject()) {
                return padrao;
            }

            JsonObject json = elemento.getAsJsonObject();

            String codigo = obterTexto(json, "error_code");

            if (codigo == null) {
                codigo = obterTexto(json, "code");
            }

            String mensagemTraduzida =
                    traduzirCodigo(codigo);

            if (mensagemTraduzida != null) {
                return mensagemTraduzida;
            }

            // O Supabase pode retornar "message", "msg"
            // ou "error_description", dependendo da resposta.
            String mensagem = obterTexto(json, "message");

            if (mensagem == null) {
                mensagem = obterTexto(json, "msg");
            }

            if (mensagem == null) {
                mensagem = obterTexto(json, "error_description");
            }

            if (mensagem != null
                    && !mensagem.trim().isEmpty()) {

                return mensagem;
            }

            return padrao;

        } catch (Exception e) {

            Log.w(
                    TAG,
                    "Não foi possível interpretar a mensagem de erro HTTP.",
                    e
            );

            return padrao;
        }
    }

    private static String obterTexto(
            JsonObject json,
            String campo
    ) {

        if (json == null || !json.has(campo)) {
            return null;
        }

        JsonElement valor = json.get(campo);

        if (valor == null
                || valor.isJsonNull()
                || !valor.isJsonPrimitive()) {

            return null;
        }

        try {
            return valor.getAsString();

        } catch (Exception e) {
            return null;
        }
    }

    private static String traduzirCodigo(String codigo) {

        if (codigo == null) {
            return null;
        }

        switch (codigo) {

            case "invalid_credentials":
                return "E-mail ou senha inválidos.";

            case "email_not_confirmed":
                return "Confirme seu e-mail antes de entrar.";

            case "user_already_exists":
            case "email_exists":
                return "Este e-mail já está cadastrado.";

            case "weak_password":
                return "Senha muito fraca. Escolha outra.";

            case "over_request_rate_limit":
                return "Muitas tentativas. Aguarde alguns minutos.";

            default:
                return null;
        }
    }
}