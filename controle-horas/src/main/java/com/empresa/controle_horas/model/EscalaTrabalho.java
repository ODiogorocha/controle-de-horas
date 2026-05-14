package com.empresa.controle_horas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "escala_trabalho")
public class EscalaTrabalho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String descricao;

    @Enumerated(EnumType.STRING)
    private TipoEscala tipo;

    // Escala semanal
    private boolean segunda;
    private boolean terca;
    private boolean quarta;
    private boolean quinta;
    private boolean sexta;
    private boolean sabado;
    private boolean domingo;

    // Escala de revezamento
    private Integer diasTrabalhados;
    private Integer diasFolga;

    // Ex: 12x36
    private Double horasPorTurno;

    public enum TipoEscala {
        SEMANAL,
        REVEZAMENTO
    }

    // =========================
    // GETTERS E SETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public TipoEscala getTipo() {
        return tipo;
    }

    public void setTipo(TipoEscala tipo) {
        this.tipo = tipo;
    }

    public boolean isSegunda() {
        return segunda;
    }

    public void setSegunda(boolean segunda) {
        this.segunda = segunda;
    }

    public boolean isTerca() {
        return terca;
    }

    public void setTerca(boolean terca) {
        this.terca = terca;
    }

    public boolean isQuarta() {
        return quarta;
    }

    public void setQuarta(boolean quarta) {
        this.quarta = quarta;
    }

    public boolean isQuinta() {
        return quinta;
    }

    public void setQuinta(boolean quinta) {
        this.quinta = quinta;
    }

    public boolean isSexta() {
        return sexta;
    }

    public void setSexta(boolean sexta) {
        this.sexta = sexta;
    }

    public boolean isSabado() {
        return sabado;
    }

    public void setSabado(boolean sabado) {
        this.sabado = sabado;
    }

    public boolean isDomingo() {
        return domingo;
    }

    public void setDomingo(boolean domingo) {
        this.domingo = domingo;
    }

    public Integer getDiasTrabalhados() {
        return diasTrabalhados;
    }

    public void setDiasTrabalhados(Integer diasTrabalhados) {
        this.diasTrabalhados = diasTrabalhados;
    }

    public Integer getDiasFolga() {
        return diasFolga;
    }

    public void setDiasFolga(Integer diasFolga) {
        this.diasFolga = diasFolga;
    }

    public Double getHorasPorTurno() {
        return horasPorTurno;
    }

    public void setHorasPorTurno(Double horasPorTurno) {
        this.horasPorTurno = horasPorTurno;
    }
}