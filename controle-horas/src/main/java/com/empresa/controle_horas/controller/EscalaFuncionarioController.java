package com.empresa.controle_horas.controller;

import com.empresa.controle_horas.dto.EscalaFuncionarioRequest;
import com.empresa.controle_horas.service.EscalaFuncionarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/escala-funcionario")
@CrossOrigin("*")
@RequiredArgsConstructor
public class EscalaFuncionarioController {

    private final EscalaFuncionarioService escalaFuncionarioService;

    @PostMapping("/salvar")
    public ResponseEntity<?> salvar(
            @RequestBody EscalaFuncionarioRequest request) {
        try {
            return ResponseEntity.ok(
                    escalaFuncionarioService.salvarEscala(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(
                    escalaFuncionarioService.buscarEscala(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/calcular")
    public ResponseEntity<?> calcular(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int dias) {
        try {
            return ResponseEntity.ok(
                    escalaFuncionarioService.calcularHorasSugeridas(id, dias));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}