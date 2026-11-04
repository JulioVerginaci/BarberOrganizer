package com.barberorganizer.barberorganizer.Repository;

import android.content.Context;

import com.barberorganizer.barberorganizer.Core.Network.AgendamentoAPI;
import com.barberorganizer.barberorganizer.Core.Network.RetrofitClient;
import com.barberorganizer.barberorganizer.Model.Agendamento;
import com.barberorganizer.barberorganizer.Model.Servico;
import com.barberorganizer.barberorganizer.Model.Usuario;
import com.google.gson.JsonArray;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRepository {

    public interface CallbackDados<T> {
        void sucesso(T dados);
        void erro(String mensagem);
    }

    private final AgendamentoAPI api;

    public AdminRepository(Context context) {
        api = RetrofitClient.getInstance(context).create(AgendamentoAPI.class);
    }

    private Map<String, Object> vazio() {
        return new HashMap<>();
    }

    public void listarAgendamentosDoDia(
            String dataIso,
            CallbackDados<List<Agendamento>> cb) {

        Map<String, Object> p = vazio();
        p.put("p_data", dataIso);

        executar(api.listarAgendamentosAdminDia(p), cb,
                "Não foi possível carregar os agendamentos de hoje.");
    }

    public void listarBarbeiros(CallbackDados<List<Usuario>> cb) {
        executar(api.listarBarbeiros(vazio()), cb,
                "Não foi possível carregar os barbeiros.");
    }

    public void listarServicos(CallbackDados<List<Servico>> cb) {
        executar(api.listarServicos(vazio()), cb,
                "Não foi possível carregar os serviços.");
    }

    public void listarClientes(CallbackDados<List<Usuario>> cb) {
        executar(api.listarClientesAdmin(vazio()), cb,
                "Não foi possível carregar os clientes.");
    }

    public void criarServico(
            String nome,
            double preco,
            int duracao,
            CallbackDados<Boolean> cb) {

        Map<String, Object> d = vazio();

        d.put("servico_id", UUID.randomUUID().toString());
        d.put("nome_servico", nome);
        d.put("preco", preco);
        d.put("duracao", duracao);

        executarEscrita(api.inserirServico(d), cb,
                "Não foi possível cadastrar o serviço.");
    }

    // Editar serviço sem mudar seu UUID.
    public void editarServico(
            String servicoId,
            String nome,
            double preco,
            int duracao,
            CallbackDados<Boolean> cb) {

        if (servicoId == null || servicoId.trim().isEmpty()) {
            cb.erro("Serviço inválido.");
            return;
        }

        if (nome == null || nome.trim().isEmpty()
                || !Double.isFinite(preco)
                || preco <= 0 || duracao <= 0) {

            cb.erro("Informe nome, preço e duração válidos.");
            return;
        }

        Map<String, Object> d = vazio();

        d.put("nome_servico", nome.trim());
        d.put("preco", preco);
        d.put("duracao", duracao);

        executarAlteracao(
                api.atualizarServico("eq." + servicoId, d),
                cb,
                "Não foi possível editar o serviço."
        );
    }

    public void excluirServico(
            String servicoId,
            CallbackDados<Boolean> cb) {

        if (servicoId == null || servicoId.trim().isEmpty()) {
            cb.erro("Serviço inválido.");
            return;
        }

        Map<String, Object> d = vazio();
        d.put("ativo", false);

        executarAlteracao(
                api.atualizarServico("eq." + servicoId, d),
                cb,
                "Não foi possível excluir o serviço."
        );
    }

    public void excluirBarbeiro(
            String barbeiroId,
            CallbackDados<Boolean> cb) {

        if (barbeiroId == null || barbeiroId.trim().isEmpty()) {
            cb.erro("Barbeiro inválido.");
            return;
        }

        Map<String, Object> d = vazio();
        d.put("ativo", false);

        executarAlteracao(
                api.atualizarBarbeiro("eq." + barbeiroId, d),
                cb,
                "Não foi possível excluir o barbeiro."
        );
    }

    private <T> void executar(
            Call<List<T>> call,
            CallbackDados<List<T>> cb,
            String padrao) {

        call.enqueue(new Callback<List<T>>() {

            @Override
            public void onResponse(
                    Call<List<T>> c,
                    Response<List<T>> r) {

                if (r.isSuccessful()) {

                    cb.sucesso(r.body() == null
                            ? Collections.emptyList()
                            : r.body());

                } else {

                    cb.erro(mensagemHttp(r, padrao));
                }
            }

            @Override
            public void onFailure(
                    Call<List<T>> c,
                    Throwable t) {

                cb.erro(padrao + " Verifique a conexão. "
                        + (t.getLocalizedMessage() == null
                        ? "" : t.getLocalizedMessage()));
            }
        });
    }

    private void executarEscrita(
            Call<Void> call,
            CallbackDados<Boolean> cb,
            String padrao) {

        call.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    Call<Void> c,
                    Response<Void> r) {

                if (r.isSuccessful()) {

                    cb.sucesso(Boolean.TRUE);

                } else {

                    cb.erro(mensagemHttp(r, padrao));
                }
            }

            @Override
            public void onFailure(
                    Call<Void> c,
                    Throwable t) {

                cb.erro(padrao + " Verifique a conexão. "
                        + (t.getLocalizedMessage() == null
                        ? "" : t.getLocalizedMessage()));
            }
        });
    }

    private void executarAlteracao(
            Call<JsonArray> call,
            CallbackDados<Boolean> cb,
            String padrao) {

        call.enqueue(new Callback<JsonArray>() {

            @Override
            public void onResponse(
                    Call<JsonArray> c,
                    Response<JsonArray> r) {

                if (!r.isSuccessful()) {
                    cb.erro(mensagemHttp(r, padrao));
                    return;
                }

                JsonArray linhas = r.body();

                if (linhas == null || linhas.size() == 0) {

                    cb.erro(padrao
                            + " Nenhum registro foi alterado."
                            + " Confirme que você está logado como administrador.");

                } else {

                    cb.sucesso(Boolean.TRUE);
                }
            }

            @Override
            public void onFailure(
                    Call<JsonArray> c,
                    Throwable t) {

                cb.erro(padrao + " Verifique a conexão. "
                        + (t.getLocalizedMessage() == null
                        ? "" : t.getLocalizedMessage()));
            }
        });
    }

    private static String mensagemHttp(
            Response<?> r,
            String padrao) {

        String detalhe = "";

        try (okhttp3.ResponseBody corpo = r.errorBody()) {

            if (corpo != null) {
                detalhe = corpo.string();
            }

        } catch (IOException ignored) {
        }

        return padrao + " HTTP " + r.code()
                + (detalhe.isEmpty() ? "." : ": " + detalhe);
    }
}