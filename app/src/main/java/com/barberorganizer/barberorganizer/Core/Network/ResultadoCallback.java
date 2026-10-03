package com.barberorganizer.barberorganizer.Core.Network;

public interface ResultadoCallback<T> {

    void onSucesso(T dados);

    void onErro(String mensagem);

    void onSessaoExpirada();

    default boolean podeReceber() {
        return true;
    }
}