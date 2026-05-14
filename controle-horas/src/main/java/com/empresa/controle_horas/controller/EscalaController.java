package com.empresa.controle_horas.controller;

import com.empresa.controle_horas.dto.EscalaRequest;
import com.empresa.controle_horas.model.EscalaAutomatica;
import com.empresa.controle_horas.service.EscalaAutomaticaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/escala")
@CrossOrigin("*")
public class EscalaController {

    private final EscalaAutomaticaService service;

    public EscalaController(EscalaAutomaticaService service) {
        this.service = service;
    }

    @PostMapping("/gerar")
    public List<EscalaAutomatica> gerarEscala(
            @RequestBody EscalaRequest request
    ) {
        return service.gerarEscala(request);
    }
}