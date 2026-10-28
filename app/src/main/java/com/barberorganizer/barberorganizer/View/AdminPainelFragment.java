package com.barberorganizer.barberorganizer.View;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.barberorganizer.R;
import com.barberorganizer.barberorganizer.Model.Agendamento;
import com.barberorganizer.barberorganizer.Repository.AdminRepository;
import com.barberorganizer.barberorganizer.Repository.AgendamentoRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AdminPainelFragment extends Fragment {
    private LinearLayout lista;
    private TextView contador;
    private AdminRepository repo;
    private int ouro, branco, cinza, fundoCartao;
    public AdminPainelFragment() { super(); }

    @Nullable @Override public View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        Context c = requireContext();
        ouro = c.getColor(R.color.gold_accent); branco = c.getColor(R.color.text_white);
        cinza = c.getColor(R.color.text_gray); fundoCartao = c.getColor(R.color.list_item_bg);
        repo = new AdminRepository(c);
        ScrollView scroll = new ScrollView(c); scroll.setFillViewport(true); scroll.setBackgroundColor(c.getColor(R.color.bg_dark));
        LinearLayout body = new LinearLayout(c); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(dp(20),dp(22),dp(20),dp(20)); scroll.addView(body);
        body.addView(texto("Painel do Administrador", 22, branco, true));
        TextView subtitulo = texto("Gestão e controle da barbearia", 13, cinza, false); body.addView(subtitulo);
        TextView tituloAcoes = texto("Ações Rápidas", 16, branco, true);
        LinearLayout.LayoutParams tituloAcoesParams = new LinearLayout.LayoutParams(-1, -2);
        tituloAcoesParams.topMargin = dp(18);
        body.addView(tituloAcoes, tituloAcoesParams);
        LinearLayout acoes = new LinearLayout(c); acoes.setOrientation(LinearLayout.HORIZONTAL); margem(acoes, 10); body.addView(acoes);
        acoes.addView(acao("♟", "Novo Barbeiro", () -> ((AdminActivity) requireActivity()).abrirCadastroBarbeiro()), new LinearLayout.LayoutParams(0, dp(82), 1));
        acoes.addView(acao("✂", "Serviços", () -> ((AdminActivity) requireActivity()).mostrarServicos()), paramsAcao());
        acoes.addView(acao("♟", "Clientes", () -> ((AdminActivity) requireActivity()).mostrarClientes()), paramsAcao());
        LinearLayout tituloAgenda = new LinearLayout(c); tituloAgenda.setGravity(Gravity.CENTER_VERTICAL); tituloAgenda.setOrientation(LinearLayout.HORIZONTAL); margem(tituloAgenda, 24); body.addView(tituloAgenda);
        tituloAgenda.addView(texto("Agendamentos do Dia", 16, branco, true), new LinearLayout.LayoutParams(0, -2, 1));
        contador = texto("Hoje", 12, ouro, true); tituloAgenda.addView(contador);
        lista = new LinearLayout(c); lista.setOrientation(LinearLayout.VERTICAL); margem(lista, 10); body.addView(lista);
        TextView carregando = texto("Carregando agendamentos...", 13, cinza, false); lista.addView(carregando);
        carregarAgenda();
        return scroll;
    }

    private void carregarAgenda() {
        String data = LocalDate.now().toString();
        repo.listarAgendamentosDoDia(data, new AdminRepository.CallbackDados<List<Agendamento>>() {
            @Override public void sucesso(List<Agendamento> dados) {
                if (!isAdded() || lista == null) return;
                lista.removeAllViews();
                if (dados == null || dados.isEmpty()) {
                    contador.setText("0 Hoje"); lista.addView(texto("Nenhum agendamento marcado para hoje.", 13, cinza, false)); return;
                }
                AgendamentoRepository.ordenarPorDataHora(dados, true);
                contador.setText(dados.size() + " Hoje");
                for (Agendamento a : dados) lista.addView(cartaoAgendamento(a));
            }
            @Override public void erro(String mensagem) {
                if (!isAdded() || lista == null) return;
                lista.removeAllViews(); lista.addView(texto(mensagem, 13, 0xFFFF8585, false));
            }
        });
    }

    private View cartaoAgendamento(Agendamento a) {
        Context c = requireContext();
        LinearLayout card = caixa(fundoCartao, 14); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(12),dp(12),dp(12),dp(12)); margem(card, 8);
        LinearLayout topo = new LinearLayout(c); topo.setGravity(Gravity.CENTER_VERTICAL); topo.setOrientation(LinearLayout.HORIZONTAL); card.addView(topo);
        String nome = vazio(a.getNomeCliente()) ? "Cliente" : a.getNomeCliente();
        TextView inicial = texto(nome.substring(0,1).toUpperCase(Locale.ROOT), 13, ouro, true); inicial.setGravity(Gravity.CENTER); inicial.setBackground(circulo());
        topo.addView(inicial, new LinearLayout.LayoutParams(dp(34),dp(34)));
        LinearLayout dados = new LinearLayout(c); dados.setOrientation(LinearLayout.VERTICAL); dados.setPadding(dp(10),0,dp(8),0);
        topo.addView(dados, new LinearLayout.LayoutParams(0,-2,1));
        dados.addView(texto(nome, 14, branco, true));
        dados.addView(texto("Barbeiro: " + (vazio(a.getNomeBarbeiro()) ? "Não informado" : a.getNomeBarbeiro()), 11, cinza, false));
        String status = vazio(a.getNomeStatus()) ? "Pendente" : a.getNomeStatus();
        TextView badge = texto(status, 10, "confirmado".equalsIgnoreCase(status) ? Color.WHITE : 0xFF222222, true); badge.setPadding(dp(8),dp(4),dp(8),dp(4)); badge.setBackground(fundoBadge("confirmado".equalsIgnoreCase(status) ? 0xFF4CAF50 : 0xFFFFB300)); topo.addView(badge);
        LinearLayout rodape = new LinearLayout(c); rodape.setGravity(Gravity.CENTER_VERTICAL); rodape.setOrientation(LinearLayout.HORIZONTAL); margem(rodape, 9); card.addView(rodape);
        String hora = AgendamentoRepository.horaParaTela(a.getHorarioAgendamento());
        rodape.addView(texto((vazio(a.getServicos()) ? "Serviço" : a.getServicos()) + " • " + hora, 11, cinza, false), new LinearLayout.LayoutParams(0,-2,1));
        rodape.addView(texto(AgendamentoRepository.dinheiro(a.getValorCobrado()), 12, ouro, true));
        return card;
    }

    private View acao(String simbolo, String nome, Runnable click) {
        LinearLayout card = caixa(0xFF2D3B44, 14); card.setOrientation(LinearLayout.VERTICAL); card.setGravity(Gravity.CENTER); card.setPadding(dp(5),dp(9),dp(5),dp(9)); card.setClickable(true); card.setFocusable(true); card.setOnClickListener(v -> click.run());
        TextView ico = texto(simbolo, 21, ouro, true); card.addView(ico);
        TextView lbl = texto(nome, 10, branco, false); lbl.setGravity(Gravity.CENTER); card.addView(lbl);
        return card;
    }
    private LinearLayout.LayoutParams paramsAcao() { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,dp(82),1); p.setMargins(dp(6),0,0,0); return p; }
    private TextView texto(String s, int sp, int color, boolean negrito) { TextView t = new TextView(requireContext()); t.setText(s); t.setTextSize(sp); t.setTextColor(color); if(negrito)t.setTypeface(null, Typeface.BOLD); return t; }
    private LinearLayout caixa(int cor, int raio) { LinearLayout l = new LinearLayout(requireContext()); GradientDrawable d = new GradientDrawable(); d.setColor(cor); d.setCornerRadius(dp(raio)); l.setBackground(d); return l; }
    private GradientDrawable circulo() { GradientDrawable d = new GradientDrawable(); d.setColor(0xFF454351); d.setShape(GradientDrawable.OVAL); return d; }
    private GradientDrawable fundoBadge(int cor) { GradientDrawable d = new GradientDrawable(); d.setColor(cor); d.setCornerRadius(dp(18)); return d; }
    private void margem(View v,int top) { ViewGroup.LayoutParams lp=v.getLayoutParams(); if(lp instanceof LinearLayout.LayoutParams){((LinearLayout.LayoutParams)lp).topMargin=dp(top);v.setLayoutParams(lp);} else {LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(top);v.setLayoutParams(p);} }
    private int dp(float value) { return (int)(value*getResources().getDisplayMetrics().density+0.5f); }
    private boolean vazio(String s) { return s==null || s.trim().isEmpty(); }
}
