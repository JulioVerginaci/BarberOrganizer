package com.barberorganizer.barberorganizer.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.barberorganizer.R;
import com.barberorganizer.barberorganizer.Controller.UsuarioController;
import com.barberorganizer.barberorganizer.Model.Resultado;

public class MainActivity extends BaseActivity {

    private EditText edt_email;
    private EditText edt_senha;

    private Button btn_logar;
    private TextView tv_cadastre_se;

    private UsuarioController usuarioController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View raiz = findViewById(R.id.main);

        if (raiz != null) {
            ViewCompat.setOnApplyWindowInsetsListener(
                    raiz,
                    (v, insets) -> {
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
                    }
            );
        }

        usuarioController = new UsuarioController(this);

        btn_logar = findViewById(R.id.btn_logar);
        edt_email = findViewById(R.id.edt_email);
        edt_senha = findViewById(R.id.edt_senha);
        tv_cadastre_se = findViewById(R.id.tv_cadastre_se);

        btn_logar.setOnClickListener(v -> fazerLogin());

        tv_cadastre_se.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    CadastroActivity.class
            );

            startActivity(intent);
        });
    }

    private void fazerLogin() {
        String email = edt_email.getText().toString().trim();
        String senha = edt_senha.getText().toString();

        edt_email.setError(null);
        edt_senha.setError(null);

        btn_logar.setEnabled(false);

        Resultado resultado = usuarioController.login(
                email,
                senha,
                new TelaCallback<String>() {

                    @Override
                    public void onSucesso(String perfil) {
                        abrirTelaDoPerfil(perfil);
                    }

                    @Override
                    public void onErro(String mensagem) {
                        btn_logar.setEnabled(true);
                        super.onErro(mensagem);
                    }

                    @Override
                    public void onSessaoExpirada() {
                        btn_logar.setEnabled(true);
                        super.onSessaoExpirada();
                    }
                }
        );

        if (!resultado.isSucesso()) {
            btn_logar.setEnabled(true);

            if (resultado.getCampo() == Resultado.Campo.EMAIL) {
                edt_email.setError(resultado.getMensagem());
                edt_email.requestFocus();

            } else if (resultado.getCampo() == Resultado.Campo.SENHA) {
                edt_senha.setError(resultado.getMensagem());
                edt_senha.requestFocus();

            } else {
                mostrarMensagem(resultado.getMensagem());
            }
        }
    }

    private void abrirTelaDoPerfil(String perfilRecebido) {
        String perfil = perfilRecebido == null
                ? ""
                : perfilRecebido.trim().toUpperCase();

        Class<?> destino;

        switch (perfil) {
            case "CLIENTE":
                destino = ClienteActivity.class;
                break;

            case "BARBEIRO":
                destino = BarbeiroActivity.class;
                break;

            case "ADMIN":
                destino = AdminActivity.class;
                break;

            default:
                usuarioController.logout();

                btn_logar.setEnabled(true);

                mostrarMensagem(
                        "O perfil da conta não foi reconhecido. "
                                + "Verifique o cadastro no banco."
                );
                return;
        }

        Intent intent = new Intent(
                MainActivity.this,
                destino
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}