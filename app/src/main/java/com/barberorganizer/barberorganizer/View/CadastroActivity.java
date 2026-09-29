package com.barberorganizer.barberorganizer.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.barberorganizer.R;
import com.barberorganizer.barberorganizer.Controller.AutenticacaoController;

public class CadastroActivity extends AppCompatActivity {

    private TextView tv_entrar;
    private EditText edt_nome, edt_telefone, edt_email, edt_senha, edt_confirmar_senha;
    private Button btn_cadastrar;

    private final AutenticacaoController controller = new AutenticacaoController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tv_entrar = findViewById(R.id.txt_entrar);
        edt_nome = findViewById(R.id.edt_nome);
        edt_telefone = findViewById(R.id.edt_telefone);
        edt_email = findViewById(R.id.edt_email);
        edt_senha = findViewById(R.id.edt_senha);
        edt_confirmar_senha = findViewById(R.id.edt_confirmar_senha);
        btn_cadastrar = findViewById(R.id.btn_cadastrar);

        tv_entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CadastroActivity.this, MainActivity.class));
                finish();
            }
        });

        btn_cadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        String erro = controller.validarCadastro(nome, telefone, email, senha, confirmarSenha);

        if (erro != null) {
            Toast.makeText(CadastroActivity.this, erro, Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(CadastroActivity.this,
                "Dados válidos! O envio para o banco será feito na próxima etapa.",
                Toast.LENGTH_LONG).show();
        startActivity(new Intent(CadastroActivity.this, MainActivity.class));
        finish();
    }
}