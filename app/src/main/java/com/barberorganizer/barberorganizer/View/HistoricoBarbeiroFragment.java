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

import java.util.List;

public class HistoricoBarbeiroFragment extends Fragment {
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View root = inflater.inflate(R.layout.fragment_historico_barbeiro, container, false);
        LinearLayout conteudo = root.findViewById(R.id.conteudoHistoricoBarbeiro);
        String id = new SessaoManager(requireContext()).getUsuarioId();
        new AgendamentoRepository(requireContext()).listarAgendamentosBarbeiro(id, true, new AgendamentoRepository.ResultadoCallback<List<Agendamento>>() {
            @Override public void onSucesso(List<Agendamento> dados) {
                if (!isAdded()) return; int n = 0;
                AgendamentoRepository.ordenarPorDataHora(dados, false);
                for (Agendamento a : dados) if (AgendamentoRepository.ehConcluido(a.getNomeStatus())) { AgendaUi.addBookingCard(requireContext(), conteudo, a, true, false, null); n++; }
                if (n == 0) conteudo.addView(AgendaUi.text(requireContext(), "Nenhum atendimento concluído no histórico.", 14, R.color.text_gray, false));
            }
            @Override public void onErro(String mensagem) { if (isAdded()) conteudo.addView(AgendaUi.text(requireContext(), mensagem, 14, R.color.status_pending, false)); }
        });
        return root;
    }
}