package com.nexo.sistema_gestao.controller;

import com.nexo.sistema_gestao.service.EstatisticaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tendencias")
@CrossOrigin(origins = "*")
public class TendenciasController {

    @Autowired
    private EstatisticaService estatisticaService;

    @GetMapping("/previsao-vendas")
    public ResponseEntity<List<Map<String, Object>>> obterPrevisaoVendas() {
        return ResponseEntity.ok(estatisticaService.calcularRegressaoLinearVendas());
    }

    @GetMapping("/cross-selling")
    public ResponseEntity<List<Map<String, String>>> obterCrossSelling() {
        return ResponseEntity.ok(estatisticaService.calcularCrossSelling());
    }

    // --- AS ROTAS QUE FALTAVAM PARA OS GRÁFICOS ATUALIZAREM ---
    @GetMapping("/curva-fluxo")
    public ResponseEntity<Map<String, Object>> obterCurvaEFluxo() {
        return ResponseEntity.ok(estatisticaService.calcularCurvaABCFluxo());
    }

    @GetMapping("/trafego")
    public ResponseEntity<Map<String, Object>> obterTrafego() {
        return ResponseEntity.ok(estatisticaService.calcularTrafego());
    }
}