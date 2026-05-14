package com.empresa.controle_horas.service;

import com.empresa.controle_horas.dto.EscalaRequest;
import com.empresa.controle_horas.model.Funcionario;
import com.empresa.controle_horas.model.Turno;
import com.empresa.controle_horas.repository.FuncionarioRepository;
import com.empresa.controle_horas.repository.TurnoRepository;
import org.springframework.stereotype.Service;
import com.empresa.controle_horas.model.EscalaAutomatica;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EscalaAutomaticaService {

    private final FuncionarioRepository funcionarioRepository;
    private final TurnoRepository turnoRepository;

    public EscalaAutomaticaService(
            FuncionarioRepository funcionarioRepository,
            TurnoRepository turnoRepository
    ) {
        this.funcionarioRepository = funcionarioRepository;
        this.turnoRepository = turnoRepository;
    }

    public List<EscalaAutomatica> gerarEscala(EscalaRequest request) {

        List<Funcionario> funcionarios = funcionarioRepository.findAll();
        List<Turno> turnos = turnoRepository.findAll();

        List<EscalaAutomatica> escalas = new ArrayList<>();

        if (funcionarios.isEmpty() || turnos.isEmpty()) {
            return escalas;
        }

        LocalDate dataAtual = request.getDataInicio();

        int indiceFuncionario = 0;

        while (!dataAtual.isAfter(request.getDataFim())) {

            for (Turno turno : turnos) {

                Funcionario funcionario =
                        funcionarios.get(indiceFuncionario % funcionarios.size());

                EscalaAutomatica escala = new EscalaAutomatica();

                escala.setFuncionario(funcionario.getNome());
                escala.setTurno(turno.getNome());
                escala.setData(dataAtual);
                escala.setHoraInicio(turno.getHoraInicio());
                escala.setHoraFim(turno.getHoraFim());
                escala.setStatus("ESCALADO");

                escalas.add(escala);

                indiceFuncionario++;
            }

            dataAtual = dataAtual.plusDays(1);
        }

        return escalas;
    }
}