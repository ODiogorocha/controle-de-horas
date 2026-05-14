package com.empresa.controle_horas.repository;

import com.empresa.controle_horas.model.EscalaTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EscalaTrabalhoRepository
        extends JpaRepository<EscalaTrabalho, Long> {

    List<EscalaTrabalho> findByFuncionarioId(Long funcionarioId);

}