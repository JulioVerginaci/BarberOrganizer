package com.barberorganizer.barberorganizer.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.barberorganizer.R;

public class AdminActivity extends BaseActivity {

    private int abaSelecionada = R.id.navPainelAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_admin);

        View raiz = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {

            Insets barras = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            v.setPadding(
                    barras.left,
                    barras.top,
                    barras.right,
                    barras.bottom
            );

            return insets;
        });

        findViewById(R.id.navPainelAdmin)
                .setOnClickListener(v -> mostrarPainel());

        findViewById(R.id.navBarbeirosAdmin)
                .setOnClickListener(v -> mostrarBarbeiros());

        findViewById(R.id.navServicosAdmin)
                .setOnClickListener(v -> mostrarServicos());

        findViewById(R.id.navPerfilAdmin)
                .setOnClickListener(v -> mostrarPerfil());

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        Fragment atual = getSupportFragmentManager()
                                .findFragmentById(
                                        R.id.fragmentContainerAdmin
                                );

                        if (!(atual instanceof AdminPainelFragment)) {

                            mostrarPainel();

                        } else {

                            setEnabled(false);

                            AdminActivity.this
                                    .getOnBackPressedDispatcher()
                                    .onBackPressed();
                        }
                    }
                }
        );

        if (savedInstanceState == null) {
            mostrarPainel();
        }
    }

    public void mostrarPainel() {

        abrir(
                R.id.navPainelAdmin,
                new AdminPainelFragment()
        );
    }

    public void mostrarBarbeiros() {

        abrir(
                R.id.navBarbeirosAdmin,
                AdminListaFragment.novaInstancia("barbeiros")
        );
    }

    public void mostrarServicos() {

        abrir(
                R.id.navServicosAdmin,
                AdminListaFragment.novaInstancia("servicos")
        );
    }

    public void mostrarPerfil() {

        abrir(
                R.id.navPerfilAdmin,
                new AdminPerfilFragment()
        );
    }

    public void mostrarClientes() {

        abrir(
                R.id.navPainelAdmin,
                AdminListaFragment.novaInstancia("clientes")
        );
    }

    public void abrirCadastroBarbeiro() {

        Intent intent = new Intent(
                this,
                CadastroUsuarioAdminActivity.class
        );

        intent.putExtra(
                "perfil_inicial",
                "BARBEIRO"
        );

        startActivity(intent);
    }

    private void abrir(int navId, Fragment fragment) {

        abaSelecionada = navId;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainerAdmin,
                        fragment
                )
                .commit();

        int[] ids = {
                R.id.navPainelAdmin,
                R.id.navBarbeirosAdmin,
                R.id.navServicosAdmin,
                R.id.navPerfilAdmin
        };

        for (int id : ids) {

            LinearLayout item = findViewById(id);

            if (item == null || item.getChildCount() < 2) {
                continue;
            }

            item.setSelected(id == abaSelecionada);

            View icone = item.getChildAt(0);

            TextView legenda = (TextView) item.getChildAt(1);

            int cor = getColor(
                    id == abaSelecionada
                            ? R.color.gold_accent
                            : R.color.text_gray
            );

            legenda.setTextColor(cor);

            if (icone instanceof android.widget.ImageView) {
                {

                    ((android.widget.ImageView) icone)
                            .setImageTintList(
                                    android.content.res.ColorStateList
                                            .valueOf(cor)
                            );
                }
            }
        }
    }}