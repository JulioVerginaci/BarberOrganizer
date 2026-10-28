package com.barberorganizer.barberorganizer.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.fragment.app.Fragment;

import com.barberorganizer.R;

public class ClienteActivity extends AppCompatActivity {
    private int selecionado = R.id.navHomeCliente;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cliente);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        findViewById(R.id.navHomeCliente).setOnClickListener(v -> mostrarHome());
        findViewById(R.id.navAgendarCliente).setOnClickListener(v -> mostrarAgendamento());
        findViewById(R.id.navAgendaCliente).setOnClickListener(v -> mostrarAgenda());
        findViewById(R.id.navHistoricoCliente).setOnClickListener(v -> mostrarHistorico());
        if (savedInstanceState == null) mostrarHome();
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.getBooleanExtra("abrir_home", false)) mostrarHome();
    }

    public void mostrarHome() { abrir(R.id.navHomeCliente, new ClienteHomeFragment()); }
    public void mostrarAgendamento() { abrir(R.id.navAgendarCliente, new NovoAgendamentoFragment()); }
    public void mostrarAgenda() { abrir(R.id.navAgendaCliente, new MeusAgendamentosFragment()); }
    public void mostrarHistorico() { abrir(R.id.navHistoricoCliente, new HistoricoClienteFragment()); }

    private void abrir(int id, Fragment fragment) {
        selecionado = id;
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainerCliente, fragment)
                .commit();
        int[] ids = {R.id.navHomeCliente, R.id.navAgendarCliente, R.id.navAgendaCliente, R.id.navHistoricoCliente};
        for (int navId : ids) {
            View item = findViewById(navId);
            if (!(item instanceof LinearLayout)) continue;
            LinearLayout linear = (LinearLayout) item;
            int cor = getColor(navId == selecionado ? R.color.gold_accent : R.color.text_gray);
            if (linear.getChildCount() > 0 && linear.getChildAt(0) instanceof ImageView) {
                ((ImageView) linear.getChildAt(0)).setColorFilter(cor);
            }
            if (linear.getChildCount() > 1 && linear.getChildAt(1) instanceof TextView) {
                ((TextView) linear.getChildAt(1)).setTextColor(cor);
            }
        }
    }
}