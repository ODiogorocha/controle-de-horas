package com.empresa.controle_horas.repository;

import com.empresa.controle_horas.model.EscalaAutomatica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EscalaAutomaticaRepository
        extends JpaRepository<EscalaAutomatica, Long> {
}