package com.barberorganizer.barberorganizer.Model;

import com.google.gson.annotations.SerializedName;

public class HorarioOcupado {
    @SerializedName("horario_agendamento") private String horario;
    @SerializedName("duracao_total") private int duracaoTotal;
    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }
    public int getDuracaoTotal() { return duracaoTotal > 0 ? duracaoTotal : 30; }
    public void setDuracaoTotal(int duracaoTotal) { this.duracaoTotal = duracaoTotal; }
}