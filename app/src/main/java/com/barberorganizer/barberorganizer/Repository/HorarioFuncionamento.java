
package com.barberorganizer.barberorganizer.Repository;

import com.barberorganizer.barberorganizer.Model.HorarioOcupado;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class HorarioFuncionamento {
    private static final LocalTime ABERTURA_MANHA = LocalTime.of(8, 0);
    private static final LocalTime FECHAMENTO_MANHA = LocalTime.of(12, 0);
    private static final LocalTime ABERTURA_TARDE = LocalTime.of(13, 30);
    private static final LocalTime FECHAMENTO_TARDE = LocalTime.of(17, 30);
    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    private HorarioFuncionamento() { }

    public static List<String> gerarSlots(LocalDate data, int duracaoMinutos,
                                          List<HorarioOcupado> ocupados) {
        return gerarSlots(data, duracaoMinutos, ocupados, LocalDateTime.now());
    }

    public static List<String> gerarSlots(LocalDate data, int duracaoMinutos,
                                          List<HorarioOcupado> ocupados,
                                          LocalDateTime agora) {
        List<String> slots = new ArrayList<>();
        if (data == null) return slots;

        int duracao = Math.max(duracaoMinutos, 1);
        List<HorarioOcupado> listaOcupados = ocupados == null
                ? new ArrayList<>() : ocupados;

        adicionarPeriodo(slots, data, ABERTURA_MANHA, FECHAMENTO_MANHA,
                duracao, listaOcupados, agora);
        adicionarPeriodo(slots, data, ABERTURA_TARDE, FECHAMENTO_TARDE,
                duracao, listaOcupados, agora);
        return slots;
    }

    private static void adicionarPeriodo(List<String> slots, LocalDate data,
                                         LocalTime abertura, LocalTime fechamento,
                                         int duracao, List<HorarioOcupado> ocupados,
                                         LocalDateTime agora) {
        for (LocalTime hora = abertura;
             !hora.plusMinutes(duracao).isAfter(fechamento);
             hora = hora.plusMinutes(30)) {

            LocalDateTime novoInicio = LocalDateTime.of(data, hora);
            LocalDateTime novoFim = novoInicio.plusMinutes(duracao);

            if (data.equals(agora.toLocalDate()) &&
                    !novoInicio.isAfter(agora)) {
                continue;
            }

            boolean conflito = false;
            for (HorarioOcupado ocupado : ocupados) {
                if (ocupado == null || ocupado.getHorario() == null ||
                        ocupado.getHorario().length() < 5) {
                    continue;
                }
                try {
                    LocalTime horaOcupada = LocalTime.parse(
                            ocupado.getHorario().substring(0, 5));
                    LocalDateTime inicioOcupado =
                            LocalDateTime.of(data, horaOcupada);
                    LocalDateTime fimOcupado = inicioOcupado.plusMinutes(
                            ocupado.getDuracaoTotal());

                    if (novoInicio.isBefore(fimOcupado) &&
                            novoFim.isAfter(inicioOcupado)) {
                        conflito = true;
                        break;
                    }
                } catch (RuntimeException ignorado) {
                    // Ignora horários inválidos.
                }
            }

            if (!conflito) slots.add(hora.format(FORMATO_HORA));
        }
    }

    public static boolean cabeNoExpediente(LocalTime inicio,
                                           int duracaoMinutos) {
        if (inicio == null || duracaoMinutos <= 0) return false;

        LocalTime fim = inicio.plusMinutes(duracaoMinutos);
        boolean manha = !inicio.isBefore(ABERTURA_MANHA)
                && !fim.isAfter(FECHAMENTO_MANHA);
        boolean tarde = !inicio.isBefore(ABERTURA_TARDE)
                && !fim.isAfter(FECHAMENTO_TARDE);

        return (manha || tarde)
                && inicio.getSecond() == 0
                && inicio.getNano() == 0
                && inicio.getMinute() % 30 == 0;
    }
}
