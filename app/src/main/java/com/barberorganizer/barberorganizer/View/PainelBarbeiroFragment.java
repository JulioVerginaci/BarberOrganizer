package com.barberorganizer.barberorganizer.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.barberorganizer.R;
import com.barberorganizer.barberorganizer.Core.Session.SessaoManager;
import com.barberorganizer.barberorganizer.Model.Agendamento;
import com.barberorganizer.barberorganizer.Repository.AgendamentoRepository;

import java.time.LocalDate;
import java.util.List;

public class PainelBarbeiroFragment extends Fragment {
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View root = inflater.inflate(R.layout.fragment_painel_barbeiro, container, false);
        LinearLayout conteudo = root.findViewById(R.id.conteudoPainelBarbeiro);
        LinearLayout resumo = root.findViewById(R.id.resumoHoje);
        String id = new SessaoManager(requireContext()).getUsuarioId();
        new AgendamentoRepository(requireContext()).listarAgendamentosBarbeiro(id, false, new AgendamentoRepository.ResultadoCallback<List<Agendamento>>() {
            @Override public void onSucesso(List<Agendamento> dados) {
                if (!isAdded()) return;
                resumo.removeAllViews();
                int total = 0;
                for (Agendamento a : dados) if (LocalDate.now().toString().equals(a.getDataAgendamento()) && AgendamentoRepository.ehAtivo(a.getNomeStatus())) total++;
                resumo.addView(AgendaUi.text(requireContext(), "AGENDA DE HOJE", 12, R.color.gold_accent, true));
                resumo.addView(AgendaUi.text(requireContext(), total + " atendimento(s) ativo(s) hoje", 16, R.color.text_white, true));
                boolean algum = false;
                for (Agendamento a : dados) {
                    if (LocalDate.now().toString().equals(a.getDataAgendamento()) && AgendamentoRepository.ehAtivo(a.getNomeStatus())) {
                        AgendaUi.addBookingCard(requireContext(), conteudo, a, true, false, null); algum = true;
                    }
                }
                if (!algum) conteudo.addView(AgendaUi.text(requireContext(), "Você não tem atendimentos ativos para hoje.", 14, R.color.text_gray, false));
            }
            @Override public void onErro(String mensagem) { if (isAdded()) conteudo.addView(AgendaUi.text(requireContext(), mensagem, 14, R.color.status_pending, false)); }
        });
        return root;
    }
}