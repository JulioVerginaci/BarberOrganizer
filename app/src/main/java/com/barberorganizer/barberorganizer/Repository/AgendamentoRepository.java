package com.barberorganizer.barberorganizer.Repository;

import android.content.Context;

import com.barberorganizer.barberorganizer.Core.Network.AgendamentoAPI;
import com.barberorganizer.barberorganizer.Core.Network.RetrofitClient;
import com.barberorganizer.barberorganizer.Model.Agendamento;
import com.barberorganizer.barberorganizer.Model.ClientePreferencia;
import com.barberorganizer.barberorganizer.Model.Corte;
import com.barberorganizer.barberorganizer.Model.HorarioOcupado;
import com.barberorganizer.barberorganizer.Model.Servico;
import com.barberorganizer.barberorganizer.Model.Usuario;
import com.barberorganizer.barberorganizer.Core.Session.SessaoManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Camada Repository: comunicação assíncrona com o Supabase através de Retrofit. */
public class AgendamentoRepository {
    public interface ResultadoCallback<T> {
        void onSucesso(T dados);
        void onErro(String mensagem);
    }

    private final AgendamentoAPI api;
    private final Context appContext;
    private final SessaoManager sessao;

    public AgendamentoRepository(Context context) {
        appContext = context.getApplicationContext();
        sessao = new SessaoManager(appContext);
        api = RetrofitClient.getInstance(appContext).create(AgendamentoAPI.class);
    }

    private Map<String, Object> vazio() { return new HashMap<>(); }

    public void listarServicos(ResultadoCallback<List<Servico>> callback) {
        executar(api.listarServicos(vazio()), callback, "Não foi possível carregar os serviços.");
    }

    public void listarBarbeiros(ResultadoCallback<List<Usuario>> callback) {
        executar(api.listarBarbeiros(vazio()), callback, "Não foi possível carregar os barbeiros.");
    }

    public void listarAgendamentosCliente(String clienteId, ResultadoCallback<List<Agendamento>> callback) {
        if (vazio(clienteId)) { callback.onErro("Não encontrei o ID do cliente na sessão. Entre novamente."); return; }
        executar(api.listarAgendamentosCliente(vazio()), callback, "Não foi possível carregar seus agendamentos.");
    }

    public void listarAgendamentosBarbeiro(String barbeiroId, boolean historico, ResultadoCallback<List<Agendamento>> callback) {
        if (vazio(barbeiroId)) { callback.onErro("Não encontrei o ID do barbeiro na sessão. Entre novamente."); return; }
        executar(api.listarAgendamentosBarbeiro(vazio()), callback, "Não foi possível carregar sua agenda.");
    }

    /** Busca os tipos de corte diretamente de public.cortes. */
    public void listarCortesDisponiveis(ResultadoCallback<List<Corte>> callback) {
        executar(
                api.listarCortesTabela("corte_favorito_id,nome_corte", "nome_corte.asc"),
                callback,
                "Não foi possível carregar a lista de cortes."
        );
    }

    /** Recupera o corte favorito através do relacionamento já existente em cliente. */
    public void buscarCorteFavorito(ResultadoCallback<Corte> callback) {
        String usuarioId = sessao.getUsuarioId();
        if (vazio(usuarioId)) {
            callback.onErro("Não encontrei o ID do cliente na sessão. Entre novamente.");
            return;
        }

        api.obterPreferenciaCliente("corte_favorito_id", "eq." + usuarioId)
                .enqueue(new Callback<List<ClientePreferencia>>() {
                    @Override
                    public void onResponse(Call<List<ClientePreferencia>> call,
                                           Response<List<ClientePreferencia>> response) {
                        if (!response.isSuccessful()) {
                            callback.onErro(erro(response,
                                    "Não foi possível consultar seu corte favorito."));
                            return;
                        }
                        List<ClientePreferencia> linhas = response.body();
                        String corteId = (linhas == null || linhas.isEmpty())
                                ? null : linhas.get(0).getCorteFavoritoId();
                        if (vazio(corteId)) {
                            callback.onSucesso(null);
                            return;
                        }
                        buscarCortePorId(corteId, callback);
                    }

                    @Override
                    public void onFailure(Call<List<ClientePreferencia>> call, Throwable t) {
                        callback.onErro(mensagemFalha(t));
                    }
                });
    }

    private void buscarCortePorId(String corteId, ResultadoCallback<Corte> callback) {
        api.obterCortePorId("corte_favorito_id,nome_corte", "eq." + corteId)
                .enqueue(new Callback<List<Corte>>() {
                    @Override
                    public void onResponse(Call<List<Corte>> call, Response<List<Corte>> response) {
                        if (!response.isSuccessful()) {
                            callback.onErro(erro(response,
                                    "Não foi possível carregar os dados do corte favorito."));
                            return;
                        }
                        List<Corte> dados = response.body();
                        callback.onSucesso(dados == null || dados.isEmpty() ? null : dados.get(0));
                    }

                    @Override
                    public void onFailure(Call<List<Corte>> call, Throwable t) {
                        callback.onErro(mensagemFalha(t));
                    }
                });
    }

    /** Atualiza apenas corte_favorito_id do cliente autenticado. */
    public void salvarCorteFavorito(String corteId, ResultadoCallback<Void> callback) {
        String usuarioId = sessao.getUsuarioId();
        if (vazio(usuarioId)) {
            callback.onErro("Não encontrei o ID do cliente na sessão. Entre novamente.");
            return;
        }
        if (vazio(corteId)) {
            callback.onErro("Selecione um corte favorito.");
            return;
        }

        Map<String, Object> dados = vazio();
        dados.put("corte_favorito_id", corteId);
        api.salvarPreferenciaCliente("eq." + usuarioId, dados)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            callback.onSucesso(null);
                        } else {
                            callback.onErro(erro(response,
                                    "Não foi possível salvar seu corte favorito. Verifique as políticas RLS da tabela cliente."));
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        callback.onErro(mensagemFalha(t));
                    }
                });
    }

    /** Lê o nome somente do registro do usuário autenticado. */
    public void buscarNomeCliente(ResultadoCallback<String> callback) {
        String usuarioId = sessao.getUsuarioId();
        if (vazio(usuarioId)) {
            callback.onErro("Não encontrei o ID do usuário na sessão. Entre novamente.");
            return;
        }
        api.obterNomeUsuario("nome", "eq." + usuarioId)
                .enqueue(new Callback<List<Usuario>>() {
                    @Override
                    public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                        if (!response.isSuccessful()) {
                            callback.onErro(erro(response,
                                    "Não foi possível carregar seu nome. Verifique a política de leitura da tabela usuario."));
                            return;
                        }
                        List<Usuario> dados = response.body();
                        callback.onSucesso(dados == null || dados.isEmpty()
                                ? null : dados.get(0).getNome());
                    }

                    @Override
                    public void onFailure(Call<List<Usuario>> call, Throwable t) {
                        callback.onErro(mensagemFalha(t));
                    }
                });
    }

    public void buscarHorariosDisponiveis(String barbeiroId, LocalDate data, int duracaoNova, ResultadoCallback<List<String>> callback) {
        if (vazio(barbeiroId) || data == null) { callback.onErro("Selecione barbeiro e data."); return; }
        if (data.isBefore(LocalDate.now())) { callback.onErro("Não é permitido agendar em uma data passada."); return; }
        Map<String, Object> p = vazio(); p.put("p_barbeiro_id", barbeiroId); p.put("p_data_agendamento", data.toString());
        api.listarHorariosOcupados(p).enqueue(new Callback<List<HorarioOcupado>>() {
            @Override public void onResponse(Call<List<HorarioOcupado>> call, Response<List<HorarioOcupado>> response) {
                if (!response.isSuccessful() || response.body() == null) { callback.onErro(erro(response, "Não foi possível consultar a disponibilidade.")); return; }
                callback.onSucesso(gerarSlots(data, duracaoNova, response.body()));
            }
            @Override public void onFailure(Call<List<HorarioOcupado>> call, Throwable t) { callback.onErro(mensagemFalha(t)); }
        });
    }

    private List<String> gerarSlots(LocalDate data, int duracaoNova, List<HorarioOcupado> ocupados) {
        List<String> slots = new ArrayList<>();
        LocalTime fechamento = LocalTime.of(18, 0);
        for (int periodo = 0; periodo < 2; periodo++) {
            LocalTime inicio = periodo == 0 ? LocalTime.of(8, 0) : LocalTime.of(13, 30);
            LocalTime ultimoInicio = periodo == 0 ? LocalTime.of(11, 30) : LocalTime.of(17, 30);
            LocalTime fimPeriodo = periodo == 0 ? LocalTime.of(12, 0) : fechamento;
            for (LocalTime hora = inicio; !hora.isAfter(ultimoInicio) && !hora.plusMinutes(Math.max(duracaoNova, 1)).isAfter(fimPeriodo); hora = hora.plusMinutes(30)) {
                LocalDateTime novoInicio = LocalDateTime.of(data, hora);
                LocalDateTime novoFim = novoInicio.plusMinutes(Math.max(duracaoNova, 1));
                if (data.equals(LocalDate.now()) && !novoInicio.isAfter(LocalDateTime.now())) continue;
                boolean conflito = false;
                for (HorarioOcupado ocupado : ocupados) {
                    try {
                        LocalTime h = LocalTime.parse(ocupado.getHorario().substring(0, 5));
                        LocalDateTime iniExistente = LocalDateTime.of(data, h);
                        LocalDateTime fimExistente = iniExistente.plusMinutes(ocupado.getDuracaoTotal());
                        if (novoInicio.isBefore(fimExistente) && novoFim.isAfter(iniExistente)) { conflito = true; break; }
                    } catch (Exception ignored) { }
                }
                if (!conflito) slots.add(hora.format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        }
        return slots;
    }

    public void criarAgendamento(String barbeiroId, String servicoId, LocalDate data, String horario, ResultadoCallback<String> callback) {
        if (vazio(barbeiroId) || vazio(servicoId) || data == null || vazio(horario)) { callback.onErro("Selecione serviço, barbeiro, data e horário."); return; }
        if (data.isBefore(LocalDate.now()) || (data.equals(LocalDate.now()) && !LocalTime.parse(horario).isAfter(LocalTime.now()))) {
            callback.onErro("Não é permitido agendar em uma data ou horário passado."); return;
        }
        Map<String, Object> params = vazio();
        params.put("p_barbeiro_id", barbeiroId); params.put("p_servico_id", servicoId);
        params.put("p_data_agendamento", data.toString()); params.put("p_horario_agendamento", horario);
        api.criarAgendamento(params).enqueue(new Callback<String>() {
            @Override public void onResponse(Call<String> call, Response<String> response) {
                if (!response.isSuccessful()) { callback.onErro(erro(response, "Não foi possível realizar o agendamento.")); return; }
                String id = response.body();
                if (id == null || id.trim().isEmpty()) { callback.onErro("O Supabase não retornou o ID do agendamento. Verifique a função criar_agendamento."); return; }
                callback.onSucesso(id.replace("\"", "").trim());
            }
            @Override public void onFailure(Call<String> call, Throwable t) { callback.onErro(mensagemFalha(t)); }
        });
    }

    public void cancelarAgendamento(String agendamentoId, ResultadoCallback<Void> callback) {
        Map<String, String> p = new HashMap<>(); p.put("p_agendamento_id", agendamentoId);
        api.cancelarAgendamento(p).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSucesso(null);
                else callback.onErro(erro(response, "Não foi possível cancelar o agendamento."));
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { callback.onErro(mensagemFalha(t)); }
        });
    }

    /** Ordena por data e hora do agendamento usando o formato ISO recebido do PostgreSQL. */
    public static void ordenarPorDataHora(List<Agendamento> lista, boolean crescente) {
        if (lista == null) return;
        Collections.sort(lista, (a, b) -> {
            String chaveA = (a.getDataAgendamento() == null ? "" : a.getDataAgendamento())
                    + "T" + (a.getHorarioAgendamento() == null ? "" : a.getHorarioAgendamento());
            String chaveB = (b.getDataAgendamento() == null ? "" : b.getDataAgendamento())
                    + "T" + (b.getHorarioAgendamento() == null ? "" : b.getHorarioAgendamento());
            return crescente ? chaveA.compareTo(chaveB) : chaveB.compareTo(chaveA);
        });
    }

    public static boolean ehAtivo(String nomeStatus) { String n = normalizar(nomeStatus); return n.equals("pendente") || n.equals("confirmado"); }
    public static boolean ehConcluido(String nomeStatus) { return normalizar(nomeStatus).equals("concluido"); }
    public static boolean ehCancelado(String nomeStatus) { return normalizar(nomeStatus).equals("cancelado"); }
    public static String dataParaTela(String data) {
        if (data == null) return "";
        try { return LocalDate.parse(data.substring(0, 10)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); }
        catch (Exception e) { return data; }
    }
    public static String horaParaTela(String hora) { return hora == null ? "" : (hora.length() >= 5 ? hora.substring(0, 5) : hora); }
    public static String dinheiro(double valor) { return String.format(new Locale("pt", "BR"), "R$ %.2f", valor); }
    private static String normalizar(String s) {
        return s == null ? "" : Normalizer.normalize(s.trim().toLowerCase(new Locale("pt", "BR")), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
    private static boolean vazio(String s) { return s == null || s.trim().isEmpty(); }
    private static String mensagemFalha(Throwable t) { return "Falha de comunicação com o Supabase: " + (t.getLocalizedMessage() == null ? "verifique a internet." : t.getLocalizedMessage()); }
    private static String erro(Response<?> r, String padrao) {
        String detalhe = "";
        try { if (r.errorBody() != null) detalhe = r.errorBody().string(); } catch (IOException ignored) { }
        return padrao + (detalhe.isEmpty() ? " Código HTTP " + r.code() + "." : " Detalhe: " + detalhe);
    }
    private static <T> void executar(Call<T> call, ResultadoCallback<T> callback, String mensagem) {
        call.enqueue(new Callback<T>() {
            @Override public void onResponse(Call<T> c, Response<T> r) {
                if (r.isSuccessful() && r.body() != null) callback.onSucesso(r.body());
                else if (r.isSuccessful()) callback.onSucesso(null);
                else callback.onErro(erro(r, mensagem));
            }
            @Override public void onFailure(Call<T> c, Throwable t) { callback.onErro(mensagemFalha(t)); }
        });
    }
}