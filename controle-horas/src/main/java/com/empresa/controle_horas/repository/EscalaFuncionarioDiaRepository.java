package com.empresa.controle_horas.repository;

import com.empresa.controle_horas.model.EscalaFuncionarioDia;
import com.empresa.controle_horas.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface EscalaFuncionarioDiaRepository
        extends JpaRepository<EscalaFuncionarioDia, Long> {

    List<EscalaFuncionarioDia> findByFuncionarioOrderByDiaSemana(
            Funcionario funcionario);

    Optional<EscalaFuncionarioDia> findByFuncionarioAndDiaSemana(
            Funcionario funcionario, DayOfWeek diaSemana);

    void deleteByFuncionario(Funcionario funcionario);
}