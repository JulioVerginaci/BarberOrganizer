package com.barberorganizer.barberorganizer.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.barberorganizer.R;
import com.barberorganizer.barberorganizer.Repository.AgendamentoRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ConfirmacaoAgendamentoActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmacao_agendamento);

        TextView barbeiro = findViewById(R.id.txtBarbeiroConfirmacao);
        TextView servico = findViewById(R.id.txtServicoConfirmacao);
        TextView data = findViewById(R.id.txtDataConfirmacao);
        TextView horario = findViewById(R.id.txtHorarioConfirmacao);
        TextView preco = findViewById(R.id.txtPrecoConfirmacao);

        barbeiro.setText(valorExtra("nome_barbeiro"));
        servico.setText(valorExtra("nome_servico"));
        data.setText(formatarData(valorExtra("data_agendamento")));
        horario.setText(AgendamentoRepository.horaParaTela(valorExtra("horario_agendamento")));
        preco.setText(valorExtra("preco"));

        findViewById(R.id.btnVoltarHomeConfirmacao).setOnClickListener(v -> irParaHomeLimpandoPilha());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { irParaHomeLimpandoPilha(); }
        });
    }

    private void irParaHomeLimpandoPilha() {
        Intent intent = new Intent(this, ClienteActivity.class);
        intent.putExtra("abrir_home", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String valorExtra(String chave) {
        String valor = getIntent().getStringExtra(chave);
        return valor == null || valor.trim().isEmpty() ? "—" : valor;
    }

    private String formatarData(String data) {
        try { return LocalDate.parse(data).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); }
        catch (Exception e) { return data == null ? "—" : data; }
    }
}