package com.barberorganizer.barberorganizer.Core.Network;

import com.barberorganizer.barberorganizer.Model.Agendamento;
import com.barberorganizer.barberorganizer.Model.ClientePreferencia;
import com.barberorganizer.barberorganizer.Model.Corte;
import com.barberorganizer.barberorganizer.Model.HorarioOcupado;
import com.barberorganizer.barberorganizer.Model.Servico;
import com.barberorganizer.barberorganizer.Model.Usuario;
import com.google.gson.JsonArray;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/** Endpoints REST e RPC usados pelo cliente, barbeiro e administrador. */
public interface AgendamentoAPI {

    // Listas usadas no agendamento (cliente) e na tela Admin.
    // Agora leem direto das tabelas e devolvem SOMENTE registros ativos (ativo = true).
    // O parâmetro Map continua existindo só para não quebrar quem já chama listarServicos(vazio()).
    @GET("rest/v1/servicos?select=servico_id,nome_servico,preco,duracao&ativo=eq.true&order=nome_servico.asc")
    Call<List<Servico>> listarServicos(@QueryMap Map<String, Object> vazio);

    @GET("rest/v1/usuario?select=id,nome,email,telefone,barbeiro!inner(ativo)&barbeiro.ativo=eq.true&order=nome.asc")
    Call<List<Usuario>> listarBarbeiros(@QueryMap Map<String, Object> vazio);

    // RPCs de agendamento já existentes no banco.
    @POST("rest/v1/rpc/listar_meus_agendamentos_cliente")
    Call<List<Agendamento>> listarAgendamentosCliente(@Body Map<String, Object> vazio);

    @POST("rest/v1/rpc/listar_minha_agenda_barbeiro")
    Call<List<Agendamento>> listarAgendamentosBarbeiro(@Body Map<String, Object> vazio);

    @POST("rest/v1/rpc/listar_horarios_ocupados")
    Call<List<HorarioOcupado>> listarHorariosOcupados(@Body Map<String, Object> parametros);

    @POST("rest/v1/rpc/criar_agendamento")
    Call<String> criarAgendamento(@Body Map<String, Object> parametros);

    @POST("rest/v1/rpc/cancelar_meu_agendamento")
    Call<Void> cancelarAgendamento(@Body Map<String, String> parametros);

    // Corte favorito: usa diretamente as tabelas existentes, sem criar funções SQL.
    @GET("rest/v1/cortes")
    Call<List<Corte>> listarCortesTabela(
            @Query("select") String campos,
            @Query("order") String ordem
    );

    @GET("rest/v1/cliente")
    Call<List<ClientePreferencia>> obterPreferenciaCliente(
            @Query("select") String campos,
            @Query("usuario_id") String filtroUsuario
    );

    @GET("rest/v1/cortes")
    Call<List<Corte>> obterCortePorId(
            @Query("select") String campos,
            @Query("corte_favorito_id") String filtroId
    );

    @Headers("Prefer: return=minimal")
    @PATCH("rest/v1/cliente")
    Call<Void> salvarPreferenciaCliente(
            @Query("usuario_id") String filtroUsuario,
            @Body Map<String, Object> dados
    );

    @GET("rest/v1/usuario")
    Call<List<Usuario>> obterNomeUsuario(
            @Query("select") String campos,
            @Query("id") String filtroId
    );

    // Consultas restritas do administrador usadas pela versão atual da tela Admin.
    @POST("rest/v1/rpc/admin_listar_agendamentos_dia")
    Call<List<Agendamento>> listarAgendamentosAdminDia(@Body Map<String, Object> parametros);

    @POST("rest/v1/rpc/admin_listar_clientes")
    Call<List<Usuario>> listarClientesAdmin(@Body Map<String, Object> vazio);

    // Gerenciamento do Admin (exclusão lógica: o registro fica no banco, só muda ativo).
    // A RLS "admin total" garante que somente o administrador consegue executar estas chamadas.
    // return=representation devolve as linhas alteradas; lista vazia = nada foi alterado.
    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/barbeiro")
    Call<JsonArray> atualizarBarbeiro(
            @Query("usuario_id") String filtroUsuario,
            @Body Map<String, Object> dados
    );

    @Headers("Prefer: return=minimal")
    @POST("rest/v1/servicos")
    Call<Void> inserirServico(@Body Map<String, Object> dados);

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/servicos")
    Call<JsonArray> atualizarServico(
            @Query("servico_id") String filtroServico,
            @Body Map<String, Object> dados
    );
}
