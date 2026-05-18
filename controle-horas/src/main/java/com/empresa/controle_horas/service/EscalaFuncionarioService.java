package com.empresa.controle_horas.service;

import com.empresa.controle_horas.dto.EscalaFuncionarioRequest;
import com.empresa.controle_horas.model.EscalaFuncionarioDia;
import com.empresa.controle_horas.model.Funcionario;
import com.empresa.controle_horas.repository.EscalaFuncionarioDiaRepository;
import com.empresa.controle_horas.repository.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EscalaFuncionarioService {

    private final FuncionarioRepository          funcionarioRepository;
    private final EscalaFuncionarioDiaRepository diaRepository;

    @Transactional
    public Map<String, Object> salvarEscala(EscalaFuncionarioRequest request) {

        Funcionario funcionario = funcionarioRepository
                .findById(request.getFuncionarioId())
                .orElseThrow(() -> new RuntimeException(
                        "Funcionario nao encontrado: " + request.getFuncionarioId()));

        // Conta quantos dias estao ativos usando isDiaAtivo()
        long diasAtivos = request.getDias().stream()
                .filter(EscalaFuncionarioRequest.DiaConfig::isDiaAtivo)
                .count();

        if (diasAtivos == 0) {
            throw new RuntimeException("Selecione pelo menos um dia de trabalho.");
        }

        double horasDiarias = funcionario.getCargo().getHorasDiarias();

        // Remove escala anterior e salva a nova
        diaRepository.deleteByFuncionario(funcionario);

        List<Map<String, String>> diasSalvos = new ArrayList<>();

        for (EscalaFuncionarioRequest.DiaConfig dia : request.getDias()) {

            EscalaFuncionarioDia entidade = new EscalaFuncionarioDia();
            entidade.setFuncionario(funcionario);
            entidade.setDiaSemana(DayOfWeek.valueOf(dia.getDiaSemana()));
            entidade.setDiaAtivo(dia.isDiaAtivo()); // usa isDiaAtivo()

            if (dia.isDiaAtivo()
                    && dia.getEntrada() != null
                    && dia.getSaida()   != null) {

                LocalTime entrada = LocalTime.parse(dia.getEntrada());
                LocalTime saida   = LocalTime.parse(dia.getSaida());
                entidade.setEntrada(entrada);
                entidade.setSaida(saida);

                double horasReais =
                        Duration.between(entrada, saida).toMinutes() / 60.0;
                entidade.setHorasDia(horasReais);

                int h = (int) horasReais;
                int m = (int) Math.round((horasReais - h) * 60);

                Map<String, String> diaInfo = new LinkedHashMap<>();
                diaInfo.put("dia",     entidade.getNomeDia());
                diaInfo.put("entrada", dia.getEntrada());
                diaInfo.put("saida",   dia.getSaida());
                diaInfo.put("horas",   h + "h" + (m > 0 ? m + "min" : ""));
                diasSalvos.add(diaInfo);

            } else {
                entidade.setHorasDia(0);
            }

            diaRepository.save(entidade);
        }

        // Total semanal real
        double totalSemanal = diasSalvos.stream().mapToDouble(d -> {
            try {
                String h = d.get("horas");
                int idxH = h.indexOf('h');
                double hrs = Double.parseDouble(h.substring(0, idxH));
                int idxM = h.indexOf("min");
                if (idxM > 0) {
                    hrs += Double.parseDouble(h.substring(idxH + 1, idxM)) / 60.0;
                }
                return hrs;
            } catch (Exception e) { return 0; }
        }).sum();

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("funcionario",  funcionario.getNome());
        resultado.put("cargo",        funcionario.getCargo().getNome());
        resultado.put("horasDiarias", String.format("%.1f h/dia", horasDiarias));
        resultado.put("diasAtivos",   diasAtivos);
        resultado.put("totalSemanal", String.format("%.2f h/semana", totalSemanal));
        resultado.put("dias",         diasSalvos);
        resultado.put("status",       "Escala salva com sucesso!");
        return resultado;
    }

    public Map<String, Object> buscarEscala(Long funcionarioId) {

        Funcionario funcionario = funcionarioRepository
                .findById(funcionarioId)
                .orElseThrow(() -> new RuntimeException(
                        "Funcionario nao encontrado: " + funcionarioId));

        List<EscalaFuncionarioDia> dias =
                diaRepository.findByFuncionarioOrderByDiaSemana(funcionario);

        if (dias.isEmpty()) {
            return Map.of(
                    "funcionario", funcionario.getNome(),
                    "mensagem",    "Nenhuma escala configurada.",
                    "dias",        List.of()
            );
        }

        List<Map<String, String>> diasInfo = new ArrayList<>();
        double totalSemanal = 0;

        for (EscalaFuncionarioDia dia : dias) {
            if (!dia.isDiaAtivo()) continue; // usa isDiaAtivo()

            int h = (int) dia.getHorasDia();
            int m = (int) Math.round((dia.getHorasDia() - h) * 60);

            Map<String, String> info = new LinkedHashMap<>();
            info.put("dia",     dia.getNomeDia());
            info.put("entrada", dia.getEntrada() != null ? dia.getEntrada().toString() : "-");
            info.put("saida",   dia.getSaida()   != null ? dia.getSaida().toString()   : "-");
            info.put("horas",   h + "h" + (m > 0 ? m + "min" : ""));
            diasInfo.add(info);
            totalSemanal += dia.getHorasDia();
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("funcionario",  funcionario.getNome());
        resultado.put("cargo",        funcionario.getCargo().getNome());
        resultado.put("totalSemanal", String.format("%.2f h/semana", totalSemanal));
        resultado.put("dias",         diasInfo);
        return resultado;
    }

    public Map<String, String> calcularHorasSugeridas(
            Long funcionarioId, int numeroDias) {

        Funcionario funcionario = funcionarioRepository
                .findById(funcionarioId)
                .orElseThrow(() -> new RuntimeException(
                        "Funcionario nao encontrado: " + funcionarioId));

        double horasDiarias  = funcionario.getCargo().getHorasDiarias();
        double horasSemanais = horasDiarias * 5;
        double horasPorDia   = numeroDias > 0 ? horasSemanais / numeroDias : horasDiarias;

        int h = (int) horasPorDia;
        int m = (int) Math.round((horasPorDia - h) * 60);

        long minutosTotais    = (long)(horasPorDia * 60);
        LocalTime saidaSugerida = LocalTime.of(8, 0).plusMinutes(minutosTotais);

        return Map.of(
                "funcionario",     funcionario.getNome(),
                "cargo",           funcionario.getCargo().getNome(),
                "horasSemanais",   String.format("%.1f h/semana", horasSemanais),
                "numeroDias",      String.valueOf(numeroDias),
                "horasPorDia",     h + "h" + (m > 0 ? m + "min" : ""),
                "entradaSugerida", "08:00",
                "saidaSugerida",   saidaSugerida.toString()
        );
    }
}