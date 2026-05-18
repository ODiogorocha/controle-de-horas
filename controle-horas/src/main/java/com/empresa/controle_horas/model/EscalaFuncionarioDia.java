package com.empresa.controle_horas.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "escala_funcionario_dia")
public class EscalaFuncionarioDia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek diaSemana;

    // Renomeado de 'ativo' para 'diaAtivo' para evitar conflito com Lombok
    @Column(name = "dia_ativo", nullable = false)
    private boolean diaAtivo = false;

    private LocalTime entrada;

    private LocalTime saida;

    private double horasDia;

    @Transient
    public String getNomeDia() {
        return switch (diaSemana) {
            case MONDAY    -> "Segunda-feira";
            case TUESDAY   -> "Terca-feira";
            case WEDNESDAY -> "Quarta-feira";
            case THURSDAY  -> "Quinta-feira";
            case FRIDAY    -> "Sexta-feira";
            case SATURDAY  -> "Sabado";
            case SUNDAY    -> "Domingo";
        };
    }
}