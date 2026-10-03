package com.barberorganizer.barberorganizer.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.barberorganizer.barberorganizer.Core.Network.ResultadoCallback;
import com.barberorganizer.barberorganizer.Core.Session.SessaoManager;

public abstract class BaseActivity extends AppCompatActivity {

    protected void mostrarMensagem(String mensagem) {
        Toast.makeText(
                this,
                mensagem,
                Toast.LENGTH_LONG
        ).show();
    }

    protected void irParaLogin(@Nullable String mensagem) {

        new SessaoManager(this).limparSessao();

        if (mensagem != null) {
            Toast.makeText(
                    getApplicationContext(),
                    mensagem,
                    Toast.LENGTH_LONG
            ).show();
        }

        Intent intent = new Intent(
                this,
                MainActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
    }

    protected abstract class TelaCallback<T>
            implements ResultadoCallback<T> {

        @Override
        public void onErro(String mensagem) {
            mostrarMensagem(mensagem);
        }

        @Override
        public void onSessaoExpirada() {
            irParaLogin(
                    "Sua sessão expirou. Entre novamente."
            );
        }

        @Override
        public boolean podeReceber() {
            return !isFinishing() && !isDestroyed();
        }
    }
}