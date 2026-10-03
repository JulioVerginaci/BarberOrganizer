package com.barberorganizer.barberorganizer.Core.Network;

public abstract class RepassadorCallback<A, B>
        implements ResultadoCallback<A> {

    protected final ResultadoCallback<B> destino;

    protected RepassadorCallback(ResultadoCallback<B> destino) {
        this.destino = destino;
    }

    @Override
    public void onErro(String mensagem) {
        destino.onErro(mensagem);
    }

    @Override
    public void onSessaoExpirada() {
        destino.onSessaoExpirada();
    }

    @Override
    public boolean podeReceber() {
        return destino.podeReceber();
    }

    // onSucesso(A dados) fica abstrato.
    // Cada utilização decide o que fazer com os dados recebidos.
}