package com.barberorganizer.barberorganizer.Model;

import com.google.gson.annotations.SerializedName;

public class Agendamento {
    @SerializedName("agendamento_id") private String id;
    @SerializedName("cliente_id") private String clienteId;
    @SerializedName("barbeiro_id") private String barbeiroId;
    @SerializedName("status_id") private String statusId;
    @SerializedName("data_agendamento") private String dataAgendamento;
    @SerializedName("horario_agendamento") private String horarioAgendamento;

    @com.google.gson.annotations.SerializedName("nome_cliente") private String nomeCliente;
    @com.google.gson.annotations.SerializedName("nome_barbeiro") private String nomeBarbeiro;
    @com.google.gson.annotations.SerializedName("nome_status") private String nomeStatus;
    private String servicos;
    @SerializedName("valor_cobrado") private double valorCobrado;
    @SerializedName("duracao_total") private int duracaoTotal;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public String getBarbeiroId() { return barbeiroId; }
    public void setBarbeiroId(String barbeiroId) { this.barbeiroId = barbeiroId; }
    public String getStatusId() { return statusId; }
    public void setStatusId(String statusId) { this.statusId = statusId; }
    public String getDataAgendamento() { return dataAgendamento; }
    public void setDataAgendamento(String dataAgendamento) { this.dataAgendamento = dataAgendamento; }
    public String getHorarioAgendamento() { return horarioAgendamento; }
    public void setHorarioAgendamento(String horarioAgendamento) { this.horarioAgendamento = horarioAgendamento; }
    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }
    public String getNomeBarbeiro() { return nomeBarbeiro; }
    public void setNomeBarbeiro(String nomeBarbeiro) { this.nomeBarbeiro = nomeBarbeiro; }
    public String getNomeStatus() { return nomeStatus; }
    public void setNomeStatus(String nomeStatus) { this.nomeStatus = nomeStatus; }
    public String getServicos() { return servicos; }
    public void setServicos(String servicos) { this.servicos = servicos; }
    public double getValorCobrado() { return valorCobrado; }
    public void setValorCobrado(double valorCobrado) { this.valorCobrado = valorCobrado; }
    public int getDuracaoTotal() { return duracaoTotal; }
    public void setDuracaoTotal(int duracaoTotal) { this.duracaoTotal = duracaoTotal; }
}