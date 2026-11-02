package com.barberorganizer.barberorganizer.View;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import com.barberorganizer.R;
import com.barberorganizer.barberorganizer.Model.Corte;
import com.barberorganizer.barberorganizer.Model.Servico;
import com.barberorganizer.barberorganizer.Model.Usuario;
import com.barberorganizer.barberorganizer.Repository.AgendamentoRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NovoAgendamentoFragment extends Fragment {

    private LinearLayout containerServicos;
    private LinearLayout containerBarbeiros;
    private LinearLayout containerDatas;
    private LinearLayout containerHorarios;

    private Button btnConfirmar;
    private TextView txtDisponibilidade;
    private TextView txtTotal;
    private TextView txtCorteFavorito;

    private final List<Servico> servicos = new ArrayList<>();
    private final List<Usuario> barbeiros = new ArrayList<>();
    private final List<String> horarios = new ArrayList<>();

    private Servico servicoSelecionado;
    private Usuario barbeiroSelecionado;
    private String horarioSelecionado;

    private LocalDate dataSelecionada = LocalDate.now();
    private AgendamentoRepository repo;

    private int versaoConsultaHorarios = 0;
    private boolean carregouServicos;
    private boolean carregouBarbeiros;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle state) {

        View root = inflater.inflate(
                R.layout.fragment_novo_agendamento,
                container,
                false
        );

        repo = new AgendamentoRepository(requireContext());

        containerServicos = root.findViewById(
                R.id.containerServicosAgendamento
        );
        containerBarbeiros = root.findViewById(
                R.id.containerBarbeirosAgendamento
        );
        containerDatas = root.findViewById(
                R.id.containerDatasAgendamento
        );
        containerHorarios = root.findViewById(
                R.id.containerHorariosAgendamento
        );

        btnConfirmar = root.findViewById(
                R.id.btnConfirmarAgendamento
        );
        txtDisponibilidade = root.findViewById(
                R.id.txtDisponibilidade
        );
        txtTotal = root.findViewById(
                R.id.txtTotalAgendamento
        );
        txtCorteFavorito = root.findViewById(
                R.id.txtCorteFavoritoAgendamento
        );

        root.findViewById(R.id.btnVoltarAgendamento)
                .setOnClickListener(v -> voltarParaHome());

        root.findViewById(R.id.btnCalendarioAgendamento)
                .setOnClickListener(v -> escolherDataCalendario());

        requireActivity().getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        voltarParaHome();
                    }
                }
        );

        btnConfirmar.setEnabled(false);
        btnConfirmar.setOnClickListener(v -> confirmar());

        construirSeletorDatas();
        carregarCorteFavorito();
        carregarServicos();
        carregarBarbeiros();

        return root;
    }

    // CARREGAR SERVIÇOS

    private void carregarServicos() {
        repo.listarServicos(
                new AgendamentoRepository.ResultadoCallback<List<Servico>>() {
                    @Override
                    public void onSucesso(List<Servico> dados) {
                        if (!isAdded()) return;

                        servicos.clear();
                        if (dados != null) {
                            servicos.addAll(dados);
                        }

                        carregouServicos = true;

                        if (servicoSelecionado == null && !servicos.isEmpty()) {
                            servicoSelecionado = servicos.get(0);
                        }

                        renderizarServicos();
                        atualizarTotal();
                        atualizarHorarios();

                        if (servicos.isEmpty()) {
                            txtDisponibilidade.setText(
                                    "Não há serviços cadastrados."
                            );
                        }
                    }

                    @Override
                    public void onErro(String mensagem) {
                        if (!isAdded()) return;

                        carregouServicos = true;
                        txtDisponibilidade.setText(mensagem);
                        atualizarHorarios();
                    }
                }
        );
    }

    // CARREGAR BARBEIROS

    private void carregarBarbeiros() {
        repo.listarBarbeiros(
                new AgendamentoRepository.ResultadoCallback<List<Usuario>>() {
                    @Override
                    public void onSucesso(List<Usuario> dados) {
                        if (!isAdded()) return;

                        barbeiros.clear();
                        if (dados != null) {
                            barbeiros.addAll(dados);
                        }

                        carregouBarbeiros = true;

                        if (barbeiroSelecionado == null && !barbeiros.isEmpty()) {
                            barbeiroSelecionado = barbeiros.get(0);
                        }

                        renderizarBarbeiros();
                        atualizarHorarios();

                        if (barbeiros.isEmpty()) {
                            txtDisponibilidade.setText(
                                    "Não há barbeiros cadastrados."
                            );
                        }
                    }

                    @Override
                    public void onErro(String mensagem) {
                        if (!isAdded()) return;

                        carregouBarbeiros = true;
                        txtDisponibilidade.setText(mensagem);
                        atualizarHorarios();
                    }
                }
        );
    }

    // CARREGAR CORTE FAVORITO

    private void carregarCorteFavorito() {
        repo.buscarCorteFavorito(
                new AgendamentoRepository.ResultadoCallback<Corte>() {
                    @Override
                    public void onSucesso(Corte corte) {
                        if (!isAdded()) return;

                        txtCorteFavorito.setText(
                                corte == null
                                        ? "Corte favorito: não definido"
                                        : "Corte favorito salvo: " + corte.getNome()
                        );
                    }

                    @Override
                    public void onErro(String mensagem) {
                        if (isAdded()) {
                            txtCorteFavorito.setText(
                                    "Corte favorito indisponível no momento."
                            );
                        }
                    }
                }
        );
    }

    // SERVIÇOS - CORES CORRIGIDAS

    private void renderizarServicos() {
        containerServicos.removeAllViews();

        for (Servico s : servicos) {

            boolean selecionado = s == servicoSelecionado;

            LinearLayout card = new LinearLayout(requireContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(Gravity.CENTER_VERTICAL);

            int pad = AgendaUi.dp(requireContext(), 12);
            card.setPadding(pad, pad, pad, pad);
            card.setBackground(selecao(selecionado));

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(
                            AgendaUi.dp(requireContext(), 116),
                            AgendaUi.dp(requireContext(), 62)
                    );

            lp.rightMargin = AgendaUi.dp(requireContext(), 8);
            card.setLayoutParams(lp);

            // FUNDO AMARELO = LETRAS ESCURAS
            // FUNDO ESCURO = LETRAS CLARAS

            TextView nome = AgendaUi.text(
                    requireContext(),
                    s.getNome(),
                    12,
                    selecionado ? R.color.bg_dark : R.color.text_white,
                    true
            );

            TextView preco = AgendaUi.text(
                    requireContext(),
                    AgendamentoRepository.dinheiro(s.getPreco()),
                    11,
                    selecionado ? R.color.bg_dark : R.color.text_gray,
                    false
            );

            card.addView(nome);
            card.addView(preco);

            card.setOnClickListener(v -> {
                servicoSelecionado = s;
                horarioSelecionado = null;

                renderizarServicos();
                atualizarTotal();
                atualizarHorarios();
            });

            containerServicos.addView(card);
        }
    }

    // BARBEIROS - CORES CORRIGIDAS

    private void renderizarBarbeiros() {
        containerBarbeiros.removeAllViews();

        for (Usuario b : barbeiros) {

            boolean selecionado = b == barbeiroSelecionado;

            LinearLayout card = new LinearLayout(requireContext());
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setOrientation(LinearLayout.HORIZONTAL);

            int pad = AgendaUi.dp(requireContext(), 12);
            card.setPadding(pad, pad, pad, pad);
            card.setBackground(selecao(selecionado));

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(
                            -1,
                            AgendaUi.dp(requireContext(), 54)
                    );

            lp.bottomMargin = AgendaUi.dp(requireContext(), 7);
            card.setLayoutParams(lp);

            TextView avatar = AgendaUi.text(
                    requireContext(),
                    "♙",
                    17,
                    selecionado ? R.color.gold_accent : R.color.text_gray,
                    true
            );

            avatar.setGravity(Gravity.CENTER);
            avatar.setBackground(circulo());

            LinearLayout.LayoutParams ap =
                    new LinearLayout.LayoutParams(
                            AgendaUi.dp(requireContext(), 32),
                            AgendaUi.dp(requireContext(), 32)
                    );

            ap.rightMargin = AgendaUi.dp(requireContext(), 12);
            card.addView(avatar, ap);

            // NOME ESCURO QUANDO SELECIONADO

            TextView nome = AgendaUi.text(
                    requireContext(),
                    b.getNome(),
                    14,
                    selecionado ? R.color.bg_dark : R.color.text_white,
                    true
            );

            card.addView(
                    nome,
                    new LinearLayout.LayoutParams(0, -2, 1f)
            );

            TextView radio = AgendaUi.text(
                    requireContext(),
                    selecionado ? "◉" : "○",
                    20,
                    selecionado ? R.color.bg_dark : R.color.text_gray,
                    true
            );

            card.addView(radio);

            card.setOnClickListener(v -> {
                barbeiroSelecionado = b;
                horarioSelecionado = null;

                renderizarBarbeiros();
                atualizarHorarios();
            });

            containerBarbeiros.addView(card);
        }
    }

    // SELETOR DE DATAS

    private void construirSeletorDatas() {
        containerDatas.removeAllViews();

        LocalDate hoje = LocalDate.now();

        LocalDate inicio = dataSelecionada.isAfter(hoje.plusDays(5))
                ? dataSelecionada
                : hoje;

        DateTimeFormatter diaSemana =
                DateTimeFormatter.ofPattern(
                        "EEE",
                        new Locale("pt", "BR")
                );

        for (int i = 0; i < 6; i++) {

            LocalDate dia = inicio.plusDays(i);
            boolean selecionado = dia.equals(dataSelecionada);

            LinearLayout chip = new LinearLayout(requireContext());
            chip.setOrientation(LinearLayout.VERTICAL);
            chip.setGravity(Gravity.CENTER);

            chip.setPadding(
                    AgendaUi.dp(requireContext(), 6),
                    AgendaUi.dp(requireContext(), 8),
                    AgendaUi.dp(requireContext(), 6),
                    AgendaUi.dp(requireContext(), 8)
            );

            chip.setBackground(selecao(selecionado));

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(
                            AgendaUi.dp(requireContext(), 52),
                            AgendaUi.dp(requireContext(), 62)
                    );

            lp.rightMargin = AgendaUi.dp(requireContext(), 7);
            chip.setLayoutParams(lp);

            TextView semana = AgendaUi.text(
                    requireContext(),
                    diaSemana.format(dia)
                            .toUpperCase(new Locale("pt", "BR"))
                            .replace(".", ""),
                    10,
                    selecionado ? R.color.bg_dark : R.color.text_gray,
                    false
            );

            semana.setGravity(Gravity.CENTER);

            TextView numero = AgendaUi.text(
                    requireContext(),
                    String.valueOf(dia.getDayOfMonth()),
                    15,
                    selecionado ? R.color.bg_dark : R.color.text_white,
                    true
            );

            numero.setGravity(Gravity.CENTER);

            chip.addView(semana);
            chip.addView(numero);

            chip.setOnClickListener(v -> {
                dataSelecionada = dia;
                horarioSelecionado = null;

                construirSeletorDatas();
                atualizarHorarios();
            });

            containerDatas.addView(chip);
        }
    }

    // VOLTAR PARA HOME

    private void voltarParaHome() {
        if (isAdded() && getActivity() instanceof ClienteActivity) {
            ((ClienteActivity) requireActivity()).mostrarHome();
        }
    }

    // CALENDÁRIO

    private void escolherDataCalendario() {
        java.util.Calendar c = java.util.Calendar.getInstance();

        c.set(
                dataSelecionada.getYear(),
                dataSelecionada.getMonthValue() - 1,
                dataSelecionada.getDayOfMonth()
        );

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (picker, year, month, day) -> {
                    LocalDate escolhida = LocalDate.of(
                            year,
                            month + 1,
                            day
                    );

                    if (escolhida.isBefore(LocalDate.now())) return;

                    dataSelecionada = escolhida;
                    horarioSelecionado = null;

                    construirSeletorDatas();
                    atualizarHorarios();
                },
                c.get(java.util.Calendar.YEAR),
                c.get(java.util.Calendar.MONTH),
                c.get(java.util.Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMinDate(
                System.currentTimeMillis() - 1000L
        );

        dialog.show();
    }

    // ATUALIZAR VALOR TOTAL

    private void atualizarTotal() {
        if (txtTotal == null) return;

        if (servicoSelecionado == null) {
            txtTotal.setText("");
            return;
        }

        txtTotal.setText(
                AgendamentoRepository.dinheiro(
                        servicoSelecionado.getPreco()
                )
                        + "  •  "
                        + servicoSelecionado.getDuracao()
                        + " min"
        );
    }

    // CONSULTAR HORÁRIOS DISPONÍVEIS

    private void atualizarHorarios() {
        if (!isAdded()
                || txtDisponibilidade == null
                || containerHorarios == null) {
            return;
        }

        // Invalida qualquer consulta anterior
        final int idConsulta = ++versaoConsultaHorarios;

        containerHorarios.removeAllViews();
        horarios.clear();
        horarioSelecionado = null;
        btnConfirmar.setEnabled(false);

        if (!carregouServicos || !carregouBarbeiros) {
            txtDisponibilidade.setText(
                    "Carregando serviços e barbeiros..."
            );
            return;
        }

        if (servicos.isEmpty()) {
            txtDisponibilidade.setText(
                    "Não há serviços cadastrados ou não foi possível carregá-los."
            );
            return;
        }

        if (barbeiros.isEmpty()) {
            txtDisponibilidade.setText(
                    "Não há barbeiros cadastrados ou não foi possível carregá-los."
            );
            return;
        }

        if (servicoSelecionado == null
                || barbeiroSelecionado == null) {
            txtDisponibilidade.setText(
                    "Selecione um serviço e um barbeiro."
            );
            return;
        }

        txtDisponibilidade.setText(
                "Consultando horários disponíveis..."
        );

        repo.buscarHorariosDisponiveis(
                barbeiroSelecionado.getId(),
                dataSelecionada,
                servicoSelecionado.getDuracao(),
                new AgendamentoRepository.ResultadoCallback<List<String>>() {

                    @Override
                    public void onSucesso(List<String> dados) {
                        if (!isAdded()
                                || idConsulta != versaoConsultaHorarios) {
                            return;
                        }

                        horarios.clear();

                        if (dados != null) {
                            horarios.addAll(dados);
                        }

                        desenharHorarios();

                        txtDisponibilidade.setText(
                                horarios.isEmpty()
                                        ? "Não há horários livres para esta data e duração."
                                        : "Selecione um dos horários disponíveis."
                        );
                    }

                    @Override
                    public void onErro(String mensagem) {
                        if (!isAdded()
                                || idConsulta != versaoConsultaHorarios) {
                            return;
                        }

                        containerHorarios.removeAllViews();
                        horarios.clear();
                        txtDisponibilidade.setText(mensagem);
                    }
                }
        );
    }

    // DESENHAR HORÁRIOS

    private void desenharHorarios() {
        containerHorarios.removeAllViews();

        if (horarios.isEmpty()) return;

        LinearLayout linha = null;

        for (int i = 0; i < horarios.size(); i++) {

            if (i % 4 == 0) {
                linha = new LinearLayout(requireContext());
                linha.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout.LayoutParams row =
                        new LinearLayout.LayoutParams(-1, -2);

                row.bottomMargin =
                        AgendaUi.dp(requireContext(), 7);

                containerHorarios.addView(linha, row);
            }

            String hora = horarios.get(i);
            boolean selecionado = hora.equals(horarioSelecionado);

            TextView chip = AgendaUi.text(
                    requireContext(),
                    hora,
                    13,
                    selecionado ? R.color.bg_dark : R.color.text_white,
                    selecionado
            );

            chip.setGravity(Gravity.CENTER);

            chip.setPadding(
                    AgendaUi.dp(requireContext(), 2),
                    0,
                    AgendaUi.dp(requireContext(), 2),
                    0
            );

            chip.setBackground(selecao(selecionado));

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(
                            0,
                            AgendaUi.dp(requireContext(), 38),
                            1f
                    );

            lp.rightMargin = AgendaUi.dp(requireContext(), 6);
            chip.setLayoutParams(lp);

            chip.setOnClickListener(v -> {
                horarioSelecionado = hora;
                desenharHorarios();
                btnConfirmar.setEnabled(true);
            });

            linha.addView(chip);
        }
    }

    // FUNDO DOS CARTÕES

    private GradientDrawable selecao(boolean selecionado) {
        GradientDrawable d = new GradientDrawable();

        d.setColor(
                requireContext().getColor(
                        selecionado
                                ? R.color.gold_accent
                                : R.color.card_background
                )
        );

        d.setCornerRadius(
                AgendaUi.dp(requireContext(), 12)
        );

        d.setStroke(
                AgendaUi.dp(
                        requireContext(),
                        selecionado ? 2 : 1
                ),
                requireContext().getColor(
                        selecionado
                                ? R.color.gold_accent
                                : R.color.input_background
                )
        );

        return d;
    }

    // CÍRCULO DO AVATAR

    private GradientDrawable circulo() {
        GradientDrawable d = new GradientDrawable();

        d.setShape(GradientDrawable.OVAL);

        d.setColor(
                requireContext().getColor(
                        R.color.input_background
                )
        );

        return d;
    }

    // CONFIRMAR AGENDAMENTO

    private void confirmar() {
        if (servicoSelecionado == null
                || barbeiroSelecionado == null
                || horarioSelecionado == null) {

            txtDisponibilidade.setText(
                    "Escolha serviço, barbeiro, data e horário."
            );
            return;
        }

        if (dataSelecionada.isBefore(LocalDate.now())
                || (dataSelecionada.equals(LocalDate.now())
                && !LocalTime.parse(horarioSelecionado)
                .isAfter(LocalTime.now()))) {

            txtDisponibilidade.setText(
                    "Selecione um horário futuro."
            );
            return;
        }

        salvar(
                servicoSelecionado,
                barbeiroSelecionado,
                dataSelecionada,
                horarioSelecionado
        );
    }

    // SALVAR NO SUPABASE

    private void salvar(
            Servico servico,
            Usuario barbeiro,
            LocalDate data,
            String horario) {

        btnConfirmar.setEnabled(false);

        txtDisponibilidade.setText(
                "Salvando agendamento..."
        );

        repo.criarAgendamento(
                barbeiro.getId(),
                servico.getId(),
                data,
                horario,
                new AgendamentoRepository.ResultadoCallback<String>() {

                    @Override
                    public void onSucesso(String id) {
                        if (!isAdded()) return;

                        Intent intent = new Intent(
                                requireContext(),
                                ConfirmacaoAgendamentoActivity.class
                        );

                        intent.putExtra(
                                "agendamento_id", id
                        );

                        intent.putExtra(
                                "nome_barbeiro",
                                barbeiro.getNome()
                        );

                        intent.putExtra(
                                "nome_servico",
                                servico.getNome()
                        );

                        intent.putExtra(
                                "data_agendamento",
                                data.toString()
                        );

                        intent.putExtra(
                                "horario_agendamento",
                                horario
                        );

                        intent.putExtra(
                                "preco",
                                AgendamentoRepository.dinheiro(
                                        servico.getPreco()
                                )
                        );

                        intent.putExtra(
                                "duracao",
                                servico.getDuracao()
                        );

                        startActivity(intent);
                    }

                    @Override
                    public void onErro(String mensagem) {
                        if (!isAdded()) return;

                        txtDisponibilidade.setText(mensagem);

                        btnConfirmar.setEnabled(
                                horarioSelecionado != null
                        );
                    }
                }
        );
    }
}
