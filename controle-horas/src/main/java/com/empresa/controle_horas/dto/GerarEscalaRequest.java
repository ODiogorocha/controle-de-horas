package com.empresa.controle_horas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class GerarEscalaRequest {

    private LocalDate dataInicio;
    private LocalDate dataFim;

    private LocalTime horarioAbertura;
    private LocalTime horarioFechamento;

    private Integer horasPorTurno;

    private String setor;

    public GerarEscalaRequest() {
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public LocalTime getHorarioAbertura() {
        return horarioAbertura;
    }

    public void setHorarioAbertura(LocalTime horarioAbertura) {
        this.horarioAbertura = horarioAbertura;
    }

    public LocalTime getHorarioFechamento() {
        return horarioFechamento;
    }

    public void setHorarioFechamento(LocalTime horarioFechamento) {
        this.horarioFechamento = horarioFechamento;
    }

    public Integer getHorasPorTurno() {
        return horasPorTurno;
    }

    public void setHorasPorTurno(Integer horasPorTurno) {
        this.horasPorTurno = horasPorTurno;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }
}