package com.empresa.controle_horas.dto;

import lombok.Data;
import java.util.List;

@Data
public class EscalaFuncionarioRequest {

    private Long funcionarioId;
    private List<DiaConfig> dias;

    @Data
    public static class DiaConfig {
        private String diaSemana;

        // Renomeado de 'ativo' para 'diaAtivo' para evitar conflito com Lombok
        private boolean diaAtivo;

        private String entrada;
        private String saida;
    }
}