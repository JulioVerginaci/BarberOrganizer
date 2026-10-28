package com.barberorganizer.barberorganizer.View;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.barberorganizer.R;
import com.barberorganizer.barberorganizer.Controller.UsuarioController;
import com.barberorganizer.barberorganizer.Model.Resultado;

public class CadastroUsuarioAdminActivity extends BaseActivity {
    private static final String TAG = "CadastroUsuarioAdmin";
    private EditText edtNome, edtTelefone, edtEmail, edtSenha;
    private RadioGroup rgPerfil;
    private Button btnSalvar, btnVoltar;
    private UsuarioController usuarioController;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); EdgeToEdge.enable(this); setContentView(R.layout.activity_cadastro_usuario_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets b=insets.getInsets(WindowInsetsCompat.Type.systemBars()); view.setPadding(b.left,b.top,b.right,b.bottom); return insets;
        });
        usuarioController=new UsuarioController(this);
        edtNome=findViewById(R.id.edt_nome_admin); edtTelefone=findViewById(R.id.edt_telefone_admin);
        edtEmail=findViewById(R.id.edt_email_admin); edtSenha=findViewById(R.id.edt_senha_admin);
        rgPerfil=findViewById(R.id.rg_perfil_admin); btnSalvar=findViewById(R.id.btn_salvar_usuario_admin); btnVoltar=findViewById(R.id.btn_voltar_admin);
        String inicial=getIntent().getStringExtra("perfil_inicial");
        if("BARBEIRO".equalsIgnoreCase(inicial)) rgPerfil.check(R.id.rb_barbeiro_admin);
        else if("ADMIN".equalsIgnoreCase(inicial)) rgPerfil.check(R.id.rb_administrador_admin);
        else rgPerfil.check(R.id.rb_cliente_admin);
        btnSalvar.setOnClickListener(v -> cadastrarUsuario()); btnVoltar.setOnClickListener(v -> finish());
    }
    private void cadastrarUsuario() {
        String nome=edtNome.getText().toString().trim(), telefone=edtTelefone.getText().toString().trim();
        String email=edtEmail.getText().toString().trim(), senha=edtSenha.getText().toString();
        if(nome.isEmpty()){edtNome.setError("Informe o nome.");edtNome.requestFocus();return;}
        if(telefone.isEmpty()){edtTelefone.setError("Informe o telefone.");edtTelefone.requestFocus();return;}
        if(email.isEmpty()){edtEmail.setError("Informe o e-mail.");edtEmail.requestFocus();return;}
        if(senha.isEmpty()){edtSenha.setError("Informe a senha.");edtSenha.requestFocus();return;}
        int id=rgPerfil.getCheckedRadioButtonId();String perfil;
        if(id==R.id.rb_cliente_admin)perfil="CLIENTE";else if(id==R.id.rb_barbeiro_admin)perfil="BARBEIRO";else if(id==R.id.rb_administrador_admin)perfil="ADMIN";else{mostrarMensagem("Selecione o perfil do usuário.");return;}
        btnSalvar.setEnabled(false); Log.d(TAG,"Cadastro administrativo solicitado. Perfil: "+perfil);
        Resultado resultado=usuarioController.cadastrarPeloAdministrador(nome,telefone,email,senha,perfil,new TelaCallback<Boolean>(){
            @Override public void onSucesso(Boolean sucesso){btnSalvar.setEnabled(true);if(Boolean.TRUE.equals(sucesso)){mostrarMensagem("Usuário cadastrado com sucesso como "+perfil+".");edtNome.setText("");edtTelefone.setText("");edtEmail.setText("");edtSenha.setText("");rgPerfil.check("BARBEIRO".equals(perfil) ? R.id.rb_barbeiro_admin : ("ADMIN".equals(perfil) ? R.id.rb_administrador_admin : R.id.rb_cliente_admin));}else mostrarMensagem("Não foi possível concluir o cadastro.");}
            @Override public void onErro(String mensagem){btnSalvar.setEnabled(true);Log.e(TAG,"Erro no cadastro administrativo: "+mensagem);super.onErro(mensagem);}
        });
        if(resultado!=null&&!resultado.isSucesso()){btnSalvar.setEnabled(true);mostrarMensagem(resultado.getMensagem());}
    }
}
