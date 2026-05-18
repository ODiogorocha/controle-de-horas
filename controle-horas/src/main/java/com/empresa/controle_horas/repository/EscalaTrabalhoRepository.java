package com.empresa.controle_horas.repository;

import com.empresa.controle_horas.model.EscalaTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EscalaTrabalhoRepository extends JpaRepository<EscalaTrabalho, Long> {

}