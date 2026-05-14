package com.empresa.controle_horas.controller;

import com.empresa.controle_horas.dto.EscalaRequest;
import com.empresa.controle_horas.service.EscalaAutomaticaService;
import org.springframework.web.bind.annotation.*;
import com.empresa.controle_horas.model.EscalaAutomatica;

import java.util.List;

@RestController
@RequestMapping("/api/escalas")
@CrossOrigin("*")
public class EscalaAutomaticaController {

    private final EscalaAutomaticaService escalaAutomaticaService;

    public EscalaAutomaticaController(
            EscalaAutomaticaService escalaAutomaticaService
    ) {
        this.escalaAutomaticaService = escalaAutomaticaService;
    }

    @PostMapping("/gerar")
    public List<EscalaAutomatica> gerarEscala(
            @RequestBody EscalaRequest request
    ) {

        return escalaAutomaticaService.gerarEscala(request);
    }
}