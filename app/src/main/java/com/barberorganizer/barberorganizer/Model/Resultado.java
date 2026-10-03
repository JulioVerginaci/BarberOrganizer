package com.barberorganizer.barberorganizer.Model;

public class Resultado {

    public enum Campo {
        NENHUM,
        NOME,
        EMAIL,
        SENHA
    }

    private final boolean sucesso;
    private final String mensagem;
    private final Campo campo;

    private Resultado(boolean sucesso, String mensagem, Campo campo) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.campo = campo;
    }

    public static Resultado ok() {
        return new Resultado(true, null, Campo.NENHUM);
    }

    public static Resultado erro(Campo campo, String mensagem) {
        return new Resultado(false, mensagem, campo);
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public Campo getCampo() {
        return campo;
    }
}