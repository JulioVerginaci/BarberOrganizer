
package com.barberorganizer.barberorganizer.View;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.barberorganizer.R;
import com.barberorganizer.barberorganizer.Core.Session.SessaoManager;
import com.barberorganizer.barberorganizer.Core.Network.AgendamentoAPI;
import com.barberorganizer.barberorganizer.Core.Network.RetrofitClient;
import com.barberorganizer.barberorganizer.Model.ClientePreferencia;
import com.barberorganizer.barberorganizer.Model.Corte;
import com.barberorganizer.barberorganizer.Model.Agendamento;
import com.barberorganizer.barberorganizer.Repository.AgendamentoRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgendaBarbeiroFragment extends Fragment {
    private LinearLayout seletorDias;
    private LinearLayout lista;
    private TextView resumoDia;
    private final List<Agendamento> agendamentos = new ArrayList<>();
    private LocalDate diaSelecionado = LocalDate.now();
    private boolean carregouDados = false;
    private AgendamentoAPI api;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View root = inflater.inflate(R.layout.fragment_agenda_barbeiro, container, false);
        seletorDias = root.findViewById(R.id.seletorDiasBarbeiro);
        lista = root.findViewById(R.id.listaAgendamentosBarbeiro);
        resumoDia = root.findViewById(R.id.txtResumoDiaBarbeiro);
        construirSeletorDias();

        api = RetrofitClient.getInstance(requireContext()).create(AgendamentoAPI.class);

        String id = new SessaoManager(requireContext()).getUsuarioId();
        new AgendamentoRepository(requireContext()).listarAgendamentosBarbeiro(id, false, new AgendamentoRepository.ResultadoCallback<List<Agendamento>>() {
            @Override public void onSucesso(List<Agendamento> dados) {
                if (!isAdded()) return;
                agendamentos.clear();
                if (dados != null) agendamentos.addAll(dados);
                carregouDados = true;
                atualizarLista();
                carregarCortesFavoritos();
            }
            @Override public void onErro(String mensagem) {
                if (!isAdded()) return;
                lista.removeAllViews();
                lista.addView(AgendaUi.text(requireContext(), mensagem, 14, R.color.status_pending, false));
            }
        });
        return root;
    }

    private void carregarCortesFavoritos() {
        Set<String> consultados = new HashSet<>();
        for (Agendamento a : agendamentos) {
            String clienteId = a.getClienteId();
            if (clienteId == null || !consultados.add(clienteId)) continue;

            api.obterPreferenciaCliente("corte_favorito_id", "eq." + clienteId)
                    .enqueue(new Callback<List<ClientePreferencia>>() {
                        @Override public void onResponse(Call<List<ClientePreferencia>> call, Response<List<ClientePreferencia>> r) {
                            if (!isAdded()) return;
                            if (!r.isSuccessful()) {
                                preencherCorte(clienteId, "Indisponível");
                                return;
                            }

                            List<ClientePreferencia> dados = r.body();
                            String corteId = dados == null || dados.isEmpty()
                                    ? null : dados.get(0).getCorteFavoritoId();

                            if (corteId == null) {
                                preencherCorte(clienteId, "Não informado");
                                return;
                            }

                            api.obterCortePorId("nome_corte,corte_favorito_id", "eq." + corteId)
                                    .enqueue(new Callback<List<Corte>>() {
                                        @Override public void onResponse(Call<List<Corte>> call, Response<List<Corte>> rc) {
                                            if (!isAdded()) return;
                                            List<Corte> cortes = rc.body();
                                            preencherCorte(clienteId,
                                                    rc.isSuccessful() && cortes != null && !cortes.isEmpty()
                                                            ? cortes.get(0).getNome()
                                                            : "Indisponível");
                                        }

                                        @Override public void onFailure(Call<List<Corte>> call, Throwable t) {
                                            if (isAdded()) preencherCorte(clienteId, "Indisponível");
                                        }
                                    });
                        }

                        @Override public void onFailure(Call<List<ClientePreferencia>> call, Throwable t) {
                            if (isAdded()) preencherCorte(clienteId, "Indisponível");
                        }
                    });
        }
    }

    private void preencherCorte(String clienteId, String nome) {
        for (Agendamento a : agendamentos) {
            if (clienteId.equals(a.getClienteId())) {
                a.setCorteFavoritoCliente(nome);
            }
        }
        atualizarLista();
    }

    private void construirSeletorDias() {
        seletorDias.removeAllViews();
        LocalDate hoje = LocalDate.now();
        LocalDate segunda = hoje.getDayOfWeek() == DayOfWeek.SUNDAY
                ? hoje.plusDays(1)
                : hoje.with(DayOfWeek.MONDAY);

        if (hoje.getDayOfWeek() == DayOfWeek.SUNDAY) diaSelecionado = segunda;
        DateTimeFormatter rotuloDia = DateTimeFormatter.ofPattern("EEE", new Locale("pt", "BR"));

        for (int i = 0; i < 6; i++) {
            LocalDate dia = segunda.plusDays(i);
            LinearLayout item = new LinearLayout(requireContext());
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);

            int padding = AgendaUi.dp(requireContext(), 10);
            item.setPadding(padding, AgendaUi.dp(requireContext(), 9), padding, AgendaUi.dp(requireContext(), 9));
            item.setBackground(fundoDia(dia.equals(diaSelecionado)));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    AgendaUi.dp(requireContext(), 52), AgendaUi.dp(requireContext(), 62));
            lp.setMargins(0, 0, AgendaUi.dp(requireContext(), 7), 0);
            item.setLayoutParams(lp);

            TextView diaSemana = AgendaUi.text(requireContext(),
                    rotuloDia.format(dia).toUpperCase(new Locale("pt", "BR")),
                    10, dia.equals(diaSelecionado) ? R.color.bg_dark : R.color.text_gray, false);
            diaSemana.setGravity(Gravity.CENTER);

            TextView numeroDia = AgendaUi.text(requireContext(),
                    String.valueOf(dia.getDayOfMonth()), 15,
                    dia.equals(diaSelecionado) ? R.color.bg_dark : R.color.text_white, true);
            numeroDia.setGravity(Gravity.CENTER);

            item.addView(diaSemana);
            item.addView(numeroDia);

            item.setOnClickListener(v -> {
                diaSelecionado = dia;
                for (int j = 0; j < seletorDias.getChildCount(); j++) {
                    View child = seletorDias.getChildAt(j);
                    LocalDate data = (LocalDate) child.getTag();
                    boolean selecionado = dia.equals(data);
                    child.setBackground(fundoDia(selecionado));
                    LinearLayout linear = (LinearLayout) child;
                    ((TextView) linear.getChildAt(0)).setTextColor(requireContext().getColor(
                            selecionado ? R.color.bg_dark : R.color.text_gray));
                    ((TextView) linear.getChildAt(1)).setTextColor(requireContext().getColor(
                            selecionado ? R.color.bg_dark : R.color.text_white));
                }
                atualizarLista();
            });

            item.setTag(dia);
            seletorDias.addView(item);
        }

        for (int j = 0; j < seletorDias.getChildCount(); j++) {
            View child = seletorDias.getChildAt(j);
            LocalDate data = (LocalDate) child.getTag();
            boolean selecionado = diaSelecionado.equals(data);
            child.setBackground(fundoDia(selecionado));
            LinearLayout linear = (LinearLayout) child;
            ((TextView) linear.getChildAt(0)).setTextColor(requireContext().getColor(
                    selecionado ? R.color.bg_dark : R.color.text_gray));
            ((TextView) linear.getChildAt(1)).setTextColor(requireContext().getColor(
                    selecionado ? R.color.bg_dark : R.color.text_white));
        }
    }

    private GradientDrawable fundoDia(boolean selecionado) {
        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(requireContext().getColor(
                selecionado ? R.color.gold_accent : R.color.card_background));
        fundo.setCornerRadius(AgendaUi.dp(requireContext(), 12));
        return fundo;
    }

    private void atualizarLista() {
        if (lista == null || !isAdded()) return;
        lista.removeAllViews();
        resumoDia.setText("ATENDIMENTOS DE " +
                diaSelecionado.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        if (!carregouDados) {
            lista.addView(AgendaUi.text(requireContext(),
                    "Carregando agenda...", 14, R.color.text_gray, false));
            return;
        }

        int total = 0;
        for (Agendamento a : agendamentos) {
            if (diaSelecionado.toString().equals(a.getDataAgendamento())
                    && AgendamentoRepository.ehAtivo(a.getNomeStatus())) {
                AgendaUi.addBookingCard(requireContext(), lista, a, true, false, null);
                total++;
            }
        }

        if (total == 0) {
            lista.addView(AgendaUi.text(requireContext(),
                    "Nenhum agendamento ativo neste dia.",
                    14, R.color.text_gray, false));
        }
    }
}
