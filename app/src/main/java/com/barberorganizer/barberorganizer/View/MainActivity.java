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

import com.barberorganizer.barberorganizer.Controller.UsuarioController;
import com.barberorganizer.barberorganizer.Model.Resultado;
import com.barberorganizer.R;

public class MainActivity extends BaseActivity {

    private EditText edt_email, edt_senha;
    private Button btn_logar;
    private TextView tv_cadastre_se;

    private UsuarioController usuarioController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // Inicializa o Controller
        usuarioController = new UsuarioController(this);

        // Componentes da tela
        btn_logar = findViewById(R.id.btn_logar);
        edt_email = findViewById(R.id.edt_email);
        edt_senha = findViewById(R.id.edt_senha);
        tv_cadastre_se = findViewById(R.id.tv_cadastre_se);

        // Botão de login
        btn_logar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fazerLogin();
            }
        });

        // Link para cadastro
        tv_cadastre_se.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(
                        MainActivity.this,
                        CadastroActivity.class
                );

                startActivity(intent);
            }
        });
    }

    private void fazerLogin() {

        // Evita dois cliques enquanto a requisição está sendo processada
        btn_logar.setEnabled(false);

        Resultado resultado = usuarioController.login(
                edt_email.getText().toString(),
                edt_senha.getText().toString(),

                new TelaCallback<Void>() {

                    @Override
                    public void onSucesso(Void dados) {

                        mostrarMensagem(
                                "Login realizado com sucesso!"
                        );

                        Intent intent = new Intent(
                                MainActivity.this,
                                ClienteActivity.class
                        );

                        startActivity(intent);

                        // Impede voltar para o login
                        finish();
                    }

                    @Override
                    public void onErro(String mensagem) {

                        btn_logar.setEnabled(true);

                        super.onErro(mensagem);
                    }
                }
        );

        // Erro de validação local
        if (!resultado.isSucesso()) {

            btn_logar.setEnabled(true);

            if (resultado.getCampo() == Resultado.Campo.EMAIL) {

                edt_email.setError(
                        resultado.getMensagem()
                );

                edt_email.requestFocus();

            } else if (resultado.getCampo() == Resultado.Campo.SENHA) {

                edt_senha.setError(
                        resultado.getMensagem()
                );

                edt_senha.requestFocus();
            }
        }
    }
}