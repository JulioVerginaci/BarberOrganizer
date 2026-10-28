package com.barberorganizer.barberorganizer.View;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.barberorganizer.R;

public class BarbeiroActivity extends AppCompatActivity {
    private int selecionado = R.id.navPainelBarbeiro;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); EdgeToEdge.enable(this); setContentView(R.layout.activity_barbeiro);
        View raiz = findViewById(R.id.main);
        if (raiz != null) ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets b = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(b.left, b.top, b.right, b.bottom); return insets;
        });
        findViewById(R.id.navPainelBarbeiro).setOnClickListener(v -> abrir(R.id.navPainelBarbeiro, new PainelBarbeiroFragment()));
        findViewById(R.id.navAgendaBarbeiro).setOnClickListener(v -> abrir(R.id.navAgendaBarbeiro, new AgendaBarbeiroFragment()));
        findViewById(R.id.navHistoricoBarbeiro).setOnClickListener(v -> abrir(R.id.navHistoricoBarbeiro, new HistoricoBarbeiroFragment()));
        if (savedInstanceState == null) abrir(R.id.navPainelBarbeiro, new PainelBarbeiroFragment());
    }
    private void abrir(int id, Fragment fragment) {
        selecionado = id;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerBarbeiro, fragment).commit();
        int[] ids = {R.id.navPainelBarbeiro, R.id.navAgendaBarbeiro, R.id.navHistoricoBarbeiro};
        String[] labels = {"Painel", "Agenda", "Histórico"};
        for (int i = 0; i < ids.length; i++) {
            LinearLayout item = findViewById(ids[i]);
            TextView label = (TextView) item.getChildAt(1);
            label.setTextColor(getColor(ids[i] == selecionado ? R.color.gold_accent : R.color.text_gray));
            item.setSelected(ids[i] == selecionado);
        }
    }
}