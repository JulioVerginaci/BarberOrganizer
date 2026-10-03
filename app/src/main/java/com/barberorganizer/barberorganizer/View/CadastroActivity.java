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
import com.barberorganizer.barberorganizer.Core.Network.ResultadoCallback;
import com.barberorganizer.barberorganizer.Model.Resultado;

public class CadastroActivity extends BaseActivity {

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

        // Controller
        usuarioController = new UsuarioController(this);

        // Componentes
        tv_entrar = findViewById(R.id.txt_entrar);

        edt_nome = findViewById(R.id.edt_nome);
        edt_telefone = findViewById(R.id.edt_telefone);
        edt_email = findViewById(R.id.edt_email);
        edt_senha = findViewById(R.id.edt_senha);
        edt_confirmar_senha = findViewById(R.id.edt_confirmar_senha);

        btn_cadastrar = findViewById(R.id.btn_cadastrar);

        // Voltar para o login
        tv_entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(
                        CadastroActivity.this,
                        MainActivity.class
                );

                startActivity(intent);
                finish();
            }
        });

        // Botão cadastrar
        btn_cadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realizarCadastro();
            }
        });
    }

    private void realizarCadastro() {

        String nome =
                edt_nome.getText().toString().trim();

        String telefone =
                edt_telefone.getText().toString().trim();

        String email =
                edt_email.getText().toString().trim();

        String senha =
                edt_senha.getText().toString();

        String confirmarSenha =
                edt_confirmar_senha.getText().toString();

        // Validação do telefone
        if (telefone.isEmpty()) {

            edt_telefone.setError(
                    "Informe o telefone."
            );

            edt_telefone.requestFocus();
            return;
        }

        // Validação da confirmação da senha
        if (confirmarSenha.isEmpty()) {

            edt_confirmar_senha.setError(
                    "Confirme a senha."
            );

            edt_confirmar_senha.requestFocus();
            return;
        }

        if (!senha.equals(confirmarSenha)) {

            edt_confirmar_senha.setError(
                    "As senhas não conferem."
            );

            edt_confirmar_senha.requestFocus();
            return;
        }

        // Desabilita o botão durante a requisição
        btn_cadastrar.setEnabled(false);

        // Cadastro através do Controller
        Resultado resultado = usuarioController.cadastrar(
                nome,
                email,
                senha,
                new TelaCallback<Boolean>() {

                    @Override
                    public void onSucesso(Boolean jaLogado) {

                        btn_cadastrar.setEnabled(true);

                        if (Boolean.TRUE.equals(jaLogado)) {

                            mostrarMensagem(
                                    "Conta criada com sucesso!"
                            );

                            Intent intent = new Intent(
                                    CadastroActivity.this,
                                    ClienteActivity.class
                            );

                            startActivity(intent);
                            finish();

                        } else {

                            mostrarMensagem(
                                    "Conta criada! Confirme seu e-mail antes de entrar."
                            );

                            Intent intent = new Intent(
                                    CadastroActivity.this,
                                    MainActivity.class
                            );

                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onErro(String mensagem) {

                        btn_cadastrar.setEnabled(true);

                        super.onErro(mensagem);
                    }
                }
        );

        // Erro de validação retornado pelo Service
        if (!resultado.isSucesso()) {

            btn_cadastrar.setEnabled(true);

            if (resultado.getCampo() == Resultado.Campo.NOME) {

                edt_nome.setError(
                        resultado.getMensagem()
                );

                edt_nome.requestFocus();

            } else if (resultado.getCampo() == Resultado.Campo.EMAIL) {

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