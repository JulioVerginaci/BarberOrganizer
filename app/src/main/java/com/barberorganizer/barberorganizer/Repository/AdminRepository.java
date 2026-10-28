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

/** Consultas e ações da área administrativa. A autorização também é validada no banco (RLS). */
public class AdminRepository {
    public interface CallbackDados<T> { void sucesso(T dados); void erro(String mensagem); }
    private final AgendamentoAPI api;
    public AdminRepository(Context context) {
        api = RetrofitClient.getInstance(context).create(AgendamentoAPI.class);
    }
    private Map<String,Object> vazio() { return new HashMap<>(); }

    public void listarAgendamentosDoDia(String dataIso, CallbackDados<List<Agendamento>> cb) {
        Map<String,Object> p = vazio(); p.put("p_data", dataIso);
        executar(api.listarAgendamentosAdminDia(p), cb, "Não foi possível carregar os agendamentos de hoje.");
    }
    /** Somente barbeiros ativos. */
    public void listarBarbeiros(CallbackDados<List<Usuario>> cb) {
        executar(api.listarBarbeiros(vazio()), cb, "Não foi possível carregar os barbeiros.");
    }
    /** Somente serviços ativos. */
    public void listarServicos(CallbackDados<List<Servico>> cb) {
        executar(api.listarServicos(vazio()), cb, "Não foi possível carregar os serviços.");
    }
    public void listarClientes(CallbackDados<List<Usuario>> cb) {
        executar(api.listarClientesAdmin(vazio()), cb, "Não foi possível carregar os clientes.");
    }

    /** Adiciona um serviço ao catálogo (ativo por padrão). */
    public void criarServico(String nome, double preco, int duracao, CallbackDados<Boolean> cb) {
        Map<String,Object> d = vazio();
        d.put("servico_id", UUID.randomUUID().toString());
        d.put("nome_servico", nome);
        d.put("preco", preco);
        d.put("duracao", duracao);
        executarEscrita(api.inserirServico(d), cb, "Não foi possível cadastrar o serviço.");
    }

    /** Exclusão lógica: o serviço some do catálogo, mas continua nos agendamentos antigos. */
    public void excluirServico(String servicoId, CallbackDados<Boolean> cb) {
        if (servicoId == null || servicoId.trim().isEmpty()) { cb.erro("Serviço inválido."); return; }
        Map<String,Object> d = vazio(); d.put("ativo", false);
        executarAlteracao(api.atualizarServico("eq." + servicoId, d), cb, "Não foi possível excluir o serviço.");
    }

    /** Exclusão lógica: o barbeiro some das listas, mas continua nos agendamentos antigos. */
    public void excluirBarbeiro(String barbeiroId, CallbackDados<Boolean> cb) {
        if (barbeiroId == null || barbeiroId.trim().isEmpty()) { cb.erro("Barbeiro inválido."); return; }
        Map<String,Object> d = vazio(); d.put("ativo", false);
        executarAlteracao(api.atualizarBarbeiro("eq." + barbeiroId, d), cb, "Não foi possível excluir o barbeiro.");
    }

    private <T> void executar(Call<T> call, CallbackDados<T> cb, String padrao) {
        call.enqueue(new Callback<T>() {
            @Override public void onResponse(Call<T> c, Response<T> r) {
                if (r.isSuccessful()) cb.sucesso(r.body() == null ? (T) Collections.emptyList() : r.body());
                else cb.erro(mensagemHttp(r, padrao));
            }
            @Override public void onFailure(Call<T> c, Throwable t) {
                cb.erro(padrao + " Verifique a conexão. " + (t.getLocalizedMessage() == null ? "" : t.getLocalizedMessage()));
            }
        });
    }

    /** INSERT: sucesso quando o HTTP é 2xx. */
    private void executarEscrita(Call<Void> call, CallbackDados<Boolean> cb, String padrao) {
        call.enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) cb.sucesso(Boolean.TRUE);
                else cb.erro(mensagemHttp(r, padrao));
            }
            @Override public void onFailure(Call<Void> c, Throwable t) {
                cb.erro(padrao + " Verifique a conexão. " + (t.getLocalizedMessage() == null ? "" : t.getLocalizedMessage()));
            }
        });
    }

    /** UPDATE: o PostgREST responde 2xx mesmo quando a RLS bloqueia, então conferimos se alguma linha mudou. */
    private void executarAlteracao(Call<JsonArray> call, CallbackDados<Boolean> cb, String padrao) {
        call.enqueue(new Callback<JsonArray>() {
            @Override public void onResponse(Call<JsonArray> c, Response<JsonArray> r) {
                if (!r.isSuccessful()) { cb.erro(mensagemHttp(r, padrao)); return; }
                JsonArray linhas = r.body();
                if (linhas == null || linhas.size() == 0) {
                    cb.erro(padrao + " Nenhum registro foi alterado. Confirme que você está logado como administrador.");
                } else {
                    cb.sucesso(Boolean.TRUE);
                }
            }
            @Override public void onFailure(Call<JsonArray> c, Throwable t) {
                cb.erro(padrao + " Verifique a conexão. " + (t.getLocalizedMessage() == null ? "" : t.getLocalizedMessage()));
            }
        });
    }

    private static String mensagemHttp(Response<?> r, String padrao) {
        String detalhe = "";
        try { if (r.errorBody() != null) detalhe = r.errorBody().string(); } catch (IOException ignored) { }
        return padrao + " HTTP " + r.code() + (detalhe.isEmpty() ? "." : ": " + detalhe);
    }
}
