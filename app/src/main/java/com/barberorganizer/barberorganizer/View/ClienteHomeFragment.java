package com.barberorganizer.barberorganizer.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.barberorganizer.R;
import com.barberorganizer.barberorganizer.Core.Session.SessaoManager;
import com.barberorganizer.barberorganizer.Model.Agendamento;
import com.barberorganizer.barberorganizer.Model.Corte;
import com.barberorganizer.barberorganizer.Repository.AgendamentoRepository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ClienteHomeFragment extends Fragment {
    private AgendamentoRepository repo;
    private LinearLayout listaProximos, listaHistorico, editorFavorito;
    private TextView titulo, corteAtual, statusProximo;
    private Spinner spCortes;
    private Button btnEditarFavorito, btnSalvarFavorito;
    private final List<Corte> cortes = new ArrayList<>();
    private String corteFavoritoId;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View root = inflater.inflate(R.layout.fragment_cliente_home, container, false);
        repo = new AgendamentoRepository(requireContext());
        titulo = root.findViewById(R.id.tituloHomeCliente);
        listaProximos = root.findViewById(R.id.listaProximosCliente);
        listaHistorico = root.findViewById(R.id.listaUltimosAtendimentosCliente);
        corteAtual = root.findViewById(R.id.txtCorteFavoritoAtual);
        statusProximo = root.findViewById(R.id.txtStatusProximoCliente);
        editorFavorito = root.findViewById(R.id.editorCorteFavorito);
        spCortes = root.findViewById(R.id.spCortesFavoritos);
        btnEditarFavorito = root.findViewById(R.id.btnEditarCorteFavorito);
        btnSalvarFavorito = root.findViewById(R.id.btnSalvarCorteFavorito);

        root.findViewById(R.id.btnIrAgendar).setOnClickListener(v -> ((ClienteActivity) requireActivity()).mostrarAgendamento());
        root.findViewById(R.id.btnEditarCorteFavorito).setOnClickListener(v -> {
            editorFavorito.setVisibility(editorFavorito.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
        btnSalvarFavorito.setOnClickListener(v -> salvarFavorito());

        carregarNome();
        carregarCortes();
        carregarAgendamentos();
        return root;
    }

    private void carregarNome() {
        repo.buscarNomeCliente(new AgendamentoRepository.ResultadoCallback<String>() {
            @Override public void onSucesso(String nome) {
                if (!isAdded() || nome == null || nome.trim().isEmpty()) return;
                titulo.setText("Olá, " + nome.trim().split(" ")[0]);
            }
            @Override public void onErro(String mensagem) { /* A Home permanece utilizável mesmo se o nome falhar. */ }
        });
    }

    private void carregarCortes() {
        repo.listarCortesDisponiveis(new AgendamentoRepository.ResultadoCallback<List<Corte>>() {
            @Override public void onSucesso(List<Corte> dados) {
                if (!isAdded()) return;
                cortes.clear();
                if (dados != null) cortes.addAll(dados);
                ArrayAdapter<Corte> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, cortes);
                adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
                spCortes.setAdapter(adapter);
                if (cortes.isEmpty()) {
                    corteAtual.setText("Nenhum corte cadastrado");
                    btnEditarFavorito.setEnabled(false);
                    return;
                }
                repo.buscarCorteFavorito(new AgendamentoRepository.ResultadoCallback<Corte>() {
                    @Override public void onSucesso(Corte corte) {
                        if (!isAdded()) return;
                        if (corte == null) {
                            corteFavoritoId = null;
                            corteAtual.setText("Nenhum corte favorito selecionado");
                        } else {
                            corteFavoritoId = corte.getId();
                            corteAtual.setText(corte.getNome());
                            for (int i = 0; i < cortes.size(); i++) {
                                if (cortes.get(i).getId() != null && cortes.get(i).getId().equals(corteFavoritoId)) {
                                    spCortes.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }
                    @Override public void onErro(String mensagem) {
                        if (isAdded()) corteAtual.setText("Não foi possível carregar o corte favorito");
                    }
                });
            }
            @Override public void onErro(String mensagem) {
                if (isAdded()) corteAtual.setText("Erro ao carregar cortes: " + mensagem);
            }
        });
    }

    private void salvarFavorito() {
        Object escolhido = spCortes.getSelectedItem();
        if (!(escolhido instanceof Corte)) {
            Toast.makeText(requireContext(), "Cadastre os tipos de corte antes de escolher um favorito.", Toast.LENGTH_LONG).show();
            return;
        }
        Corte corte = (Corte) escolhido;
        btnSalvarFavorito.setEnabled(false);
        repo.salvarCorteFavorito(corte.getId(), new AgendamentoRepository.ResultadoCallback<Void>() {
            @Override public void onSucesso(Void dados) {
                if (!isAdded()) return;
                corteFavoritoId = corte.getId();
                corteAtual.setText(corte.getNome());
                editorFavorito.setVisibility(View.GONE);
                btnSalvarFavorito.setEnabled(true);
                Toast.makeText(requireContext(), "Corte favorito salvo.", Toast.LENGTH_SHORT).show();
            }
            @Override public void onErro(String mensagem) {
                if (!isAdded()) return;
                btnSalvarFavorito.setEnabled(true);
                Toast.makeText(requireContext(), mensagem, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void carregarAgendamentos() {
        String uid = new SessaoManager(requireContext()).getUsuarioId();
        repo.listarAgendamentosCliente(uid, new AgendamentoRepository.ResultadoCallback<List<Agendamento>>() {
            @Override public void onSucesso(List<Agendamento> dados) {
                if (!isAdded()) return;
                listaProximos.removeAllViews();
                listaHistorico.removeAllViews();
                List<Agendamento> lista = dados == null ? new ArrayList<>() : dados;
                AgendamentoRepository.ordenarPorDataHora(lista, true);
                int proximos = 0;
                for (Agendamento a : lista) {
                    if (AgendamentoRepository.ehAtivo(a.getNomeStatus()) && futuro(a)) {
                        adicionarLinhaProximo(a);
                        statusProximo.setText("● " + (a.getNomeStatus() == null ? "Confirmado" : a.getNomeStatus()));
                        proximos++;
                        if (proximos == 1) break;
                    }
                }
                if (proximos == 0) {
                    statusProximo.setText("");
                    listaProximos.addView(AgendaUi.text(requireContext(), "Você ainda não tem agendamentos futuros.", 13, R.color.text_gray, false));
                }
                AgendamentoRepository.ordenarPorDataHora(lista, false);
                int recentes = 0;
                for (Agendamento a : lista) {
                    if (AgendamentoRepository.ehConcluido(a.getNomeStatus())) {
                        adicionarLinhaHistorico(a);
                        recentes++;
                        if (recentes == 3) break;
                    }
                }
                if (recentes == 0) listaHistorico.addView(AgendaUi.text(requireContext(), "Seus atendimentos concluídos aparecerão aqui.", 13, R.color.text_gray, false));
            }
            @Override public void onErro(String mensagem) {
                if (!isAdded()) return;
                listaProximos.addView(AgendaUi.text(requireContext(), mensagem, 13, R.color.status_pending, false));
            }
        });
    }

    private void adicionarLinhaProximo(Agendamento a) {
        LinearLayout linha = new LinearLayout(requireContext());
        linha.setOrientation(LinearLayout.VERTICAL);
        linha.setPadding(0, AgendaUi.dp(requireContext(), 3), 0, AgendaUi.dp(requireContext(), 3));
        adicionarPar(linha, "♙  Barbeiro:", a.getNomeBarbeiro());
        adicionarPar(linha, "✂  Serviço:", a.getServicos());
        adicionarPar(linha, "▣  Data:", AgendamentoRepository.dataParaTela(a.getDataAgendamento()));
        adicionarPar(linha, "◷  Horário:", AgendamentoRepository.horaParaTela(a.getHorarioAgendamento()));
        listaProximos.addView(linha);
    }

    private void adicionarPar(LinearLayout pai, String chave, String valor) {
        LinearLayout linha = new LinearLayout(requireContext());
        linha.setGravity(Gravity.CENTER_VERTICAL);
        linha.setOrientation(LinearLayout.HORIZONTAL);
        TextView esquerda = AgendaUi.text(requireContext(), chave, 13, R.color.text_gray, false);
        TextView direita = AgendaUi.text(requireContext(), valor == null ? "—" : valor, 13, R.color.text_white, true);
        direita.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        linha.addView(esquerda, new LinearLayout.LayoutParams(0, -2, 1f));
        linha.addView(direita, new LinearLayout.LayoutParams(0, -2, 1.4f));
        pai.addView(linha);
    }

    private void adicionarLinhaHistorico(Agendamento a) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(AgendaUi.dp(requireContext(), 12), AgendaUi.dp(requireContext(), 10), AgendaUi.dp(requireContext(), 12), AgendaUi.dp(requireContext(), 10));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(requireContext().getColor(R.color.card_background));
        bg.setCornerRadius(AgendaUi.dp(requireContext(), 12));
        card.setBackground(bg);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.bottomMargin = AgendaUi.dp(requireContext(), 8);
        card.setLayoutParams(cp);
        TextView icon = AgendaUi.text(requireContext(), "✂", 17, R.color.gold_accent, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(round(R.color.input_background));
        card.addView(icon, new LinearLayout.LayoutParams(AgendaUi.dp(requireContext(), 34), AgendaUi.dp(requireContext(), 34)));
        LinearLayout textos = new LinearLayout(requireContext());
        textos.setOrientation(LinearLayout.VERTICAL);
        textos.setPadding(AgendaUi.dp(requireContext(), 10), 0, AgendaUi.dp(requireContext(), 4), 0);
        TextView serv = AgendaUi.text(requireContext(), a.getServicos(), 13, R.color.text_white, true);
        TextView dt = AgendaUi.text(requireContext(), AgendamentoRepository.dataParaTela(a.getDataAgendamento()), 11, R.color.text_gray, false);
        textos.addView(serv); textos.addView(dt);
        card.addView(textos, new LinearLayout.LayoutParams(0, -2, 1));
        TextView preco = AgendaUi.text(requireContext(), AgendamentoRepository.dinheiro(a.getValorCobrado()), 13, R.color.gold_accent, true);
        card.addView(preco);
        listaHistorico.addView(card);
    }

    private GradientDrawable round(int colorId) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(requireContext().getColor(colorId));
        d.setCornerRadius(AgendaUi.dp(requireContext(), 12));
        return d;
    }

    static boolean futuro(Agendamento a) {
        try {
            String hora = a.getHorarioAgendamento();
            if (hora.length() > 5) hora = hora.substring(0, 5);
            return LocalDateTime.of(LocalDate.parse(a.getDataAgendamento()), LocalTime.parse(hora)).isAfter(LocalDateTime.now());
        } catch (Exception e) { return false; }
    }
}