package com.barberorganizer.barberorganizer.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class CadastroActivity extends BaseActivity {

    private static final String TAG = "CadastroActivity";

    private TextView tv_entrar;

    private EditText edt_nome;
    private EditText edt_telefone;
    private EditText edt_email;
    private EditText edt_senha;
    private EditText edt_confirmar_senha;

    private Button btn_cadastrar;

    private UsuarioController usuarioController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(
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

        // Inicializa o Controller.
        usuarioController = new UsuarioController(this);

        // Inicializa os componentes da tela.
        tv_entrar = findViewById(R.id.txt_entrar);
        edt_nome = findViewById(R.id.edt_nome);
        edt_telefone = findViewById(R.id.edt_telefone);
        edt_email = findViewById(R.id.edt_email);
        edt_senha = findViewById(R.id.edt_senha);
        edt_confirmar_senha = findViewById(R.id.edt_confirmar_senha);

        btn_cadastrar = findViewById(R.id.btn_cadastrar);

        // Link para retornar à tela de login.
        tv_entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirTelaLogin();
            }
        });

        // Evento do botão Cadastrar.
        btn_cadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Botão Cadastrar clicado.");
                realizarCadastro();
            }
        });
    }

    private void realizarCadastro() {

        String nome = edt_nome.getText().toString().trim();

        String telefone = edt_telefone.getText().toString().trim();

        String email = edt_email.getText().toString().trim();

        String senha = edt_senha.getText().toString();

        String confirmarSenha = edt_confirmar_senha.getText().toString();

        // Validação do telefone.
        if (telefone.isEmpty()) {
            edt_telefone.setError("Informe o telefone.");
            edt_telefone.requestFocus();
            return;
        }

        // Validação da confirmação de senha.
        if (confirmarSenha.isEmpty()) {
            edt_confirmar_senha.setError("Confirme a senha.");
            edt_confirmar_senha.requestFocus();
            return;
        }

        // Verifica se as senhas são iguais.
        if (!senha.equals(confirmarSenha)) {
            edt_confirmar_senha.setError("As senhas não conferem.");
            edt_confirmar_senha.requestFocus();
            return;
        }

        // Impede cliques duplicados durante a requisição.
        btn_cadastrar.setEnabled(false);

        Log.d(
                TAG,
                "Validação local concluída. Enviando cadastro ao controller."
        );

        Resultado resultado = usuarioController.cadastrar(
                nome,
                telefone,
                email,
                senha,
                new TelaCallback<Boolean>() {

                    @Override
                    public void onSucesso(Boolean sucesso) {

                        Log.d(
                                TAG,
                                "Callback de cadastro recebido. Sucesso: "
                                        + sucesso
                        );

                        btn_cadastrar.setEnabled(true);

                        if (Boolean.TRUE.equals(sucesso)) {

                            // Limpa a sessão local para que o usuário
                            // entre pela tela de login.
                            usuarioController.logout();

                            mostrarMensagem(
                                    "Cliente cadastrado com sucesso! "
                                            + "Faça login para continuar."
                            );

                            // Volta ao login e limpa o histórico
                            // de navegação.
                            abrirTelaLoginLimpandoHistorico();

                        } else {

                            mostrarMensagem(
                                    "Não foi possível concluir o cadastro."
                            );
                        }
                    }

                    @Override
                    public void onErro(String mensagem) {

                        Log.e(TAG, "Falha no cadastro: " + mensagem);

                        btn_cadastrar.setEnabled(true);

                        // Mostra a mensagem de erro padrão da BaseActivity.
                        super.onErro(mensagem);
                    }
                }
        );

        // Trata erros de validação retornados imediatamente pelo Service.
        if (!resultado.isSucesso()) {

            btn_cadastrar.setEnabled(true);

            exibirErroValidacao(resultado);
        }
    }

    private void exibirErroValidacao(Resultado resultado) {

        String mensagem = resultado.getMensagem();

        if (resultado.getCampo() == Resultado.Campo.NOME) {

            edt_nome.setError(mensagem);
            edt_nome.requestFocus();

        } else if (resultado.getCampo() == Resultado.Campo.EMAIL) {

            edt_email.setError(mensagem);
            edt_email.requestFocus();

        } else if (resultado.getCampo() == Resultado.Campo.SENHA) {

            edt_senha.setError(mensagem);
            edt_senha.requestFocus();

        } else {

            mostrarMensagem(
                    mensagem != null
                            ? mensagem
                            : "Não foi possível validar o cadastro."
            );
        }
    }

    private void abrirTelaLogin() {

        Intent intent = new Intent(
                CadastroActivity.this,
                MainActivity.class
        );

        startActivity(intent);
        finish();
    }

    private void abrirTelaLoginLimpandoHistorico() {

        Intent intent = new Intent(
                CadastroActivity.this,
                MainActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}