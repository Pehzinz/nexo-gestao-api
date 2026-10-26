package com.nexo.sistema_gestao.controller;

import com.nexo.sistema_gestao.model.Evento;
import com.nexo.sistema_gestao.model.Produto;
import com.nexo.sistema_gestao.model.Venda;
import com.nexo.sistema_gestao.repository.EventoRepository;
import com.nexo.sistema_gestao.repository.ProdutoRepository;
import com.nexo.sistema_gestao.repository.VendaRepository;
import com.nexo.sistema_gestao.service.EstatisticaService;
import com.nexo.sistema_gestao.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agente")
@CrossOrigin(origins = "*")
public class GeminiController {

    @Autowired private GeminiService geminiService;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private VendaRepository vendaRepository;
    @Autowired private EventoRepository eventoRepository;
    
    // NOVO: Importando o Motor Matemático para a Web
    @Autowired private EstatisticaService estatisticaService;

    // Palavras-chave permitidas para o contexto de negócios
    private static final List<String> PALAVRAS_CHAVE_NEGOCIOS = Arrays.asList(
        "venda", "estoque", "produto", "lucro", "faturamento", "cliente", "nexo", "gestão", 
        "promo", "preço", "caixa", "relatório", "ajuda", "olá", "oi", "bom dia", "boa tarde", 
        "tendência", "sistema", "desconto", "comprar", "vender", "agendar", "marcar", "lembrar", "reunião", "evento", "abc", "curva"
    );

    @PostMapping("/perguntar")
    public ResponseEntity<?> perguntarAoAgente(@RequestBody Map<String, String> payload) {
        String perguntaUsuario = payload.get("pergunta");

        if (perguntaUsuario == null || perguntaUsuario.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "A pergunta não pode estar vazia."));
        }

        // --- FILTRO DE CONTEXTO ULTRA RÁPIDO ---
        String perguntaLower = perguntaUsuario.toLowerCase();
        boolean temContexto = PALAVRAS_CHAVE_NEGOCIOS.stream().anyMatch(perguntaLower::contains);
        
        if (!temContexto) {
            return ResponseEntity.ok(Map.of("resposta", "Sou o Consultor Estratégico do Nexo Gestão. Fui treinado exclusivamente para analisar o estoque, faturamento, inteligência de mercado e agendar compromissos. Por favor, pergunte algo relacionado aos nossos negócios!"));
        }

        // 1. Coleta os dados Básicos
        List<Produto> produtos = produtoRepository.findAll();
        List<Venda> vendas = vendaRepository.findAll();
        BigDecimal faturamento = BigDecimal.ZERO;
        for (Venda v : vendas) {
            if (v.getTotal() != null) {
                faturamento = faturamento.add(v.getTotal());
            }
        }

        // 2. Extração de Métricas Avançadas (Data Science)
        Map<String, Object> curvaEFluxo = estatisticaService.calcularCurvaABCFluxo();
        @SuppressWarnings("unchecked") List<Map<String, Object>> curvaABC = (List<Map<String, Object>>) curvaEFluxo.get("curva");
        String produtoAltaTendencia = (String) curvaEFluxo.get("produtoAltaTendencia");
        String produtoParado = (String) curvaEFluxo.get("produtoParado");

        Map<String, Object> trafegoData = estatisticaService.calcularTrafego();
        @SuppressWarnings("unchecked") List<Map<String, Object>> trafego = (List<Map<String, Object>>) trafegoData.get("grafico");
        
        List<Map<String, Object>> previsoes = estatisticaService.calcularRegressaoLinearVendas();
        List<Map<String, String>> crossSelling = estatisticaService.calcularCrossSelling();

        // 3. Monta o Dossiê para o Gemini
        StringBuilder contexto = new StringBuilder();
        contexto.append("Você é o Consultor Estratégico do sistema 'Nexo Gestão'. ");
        contexto.append("Seja direto, rápido e foque em respostas curtas e profissionais. ");
        contexto.append("REGRA DE CALENDÁRIO: Se o usuário pedir para agendar, marcar ou lembrar de um evento/compromisso, responda EXATAMENTE neste formato (sem textos extras): [AGENDAR: Titulo | AAAA-MM-DD | HH:MM | Descricao breve]\n\n");
        
        contexto.append("📊 DADOS FINANCEIROS:\n");
        contexto.append("- Faturamento Total: R$ ").append(String.format("%.2f", faturamento.doubleValue())).append("\n");
        contexto.append("- Total de Pedidos Fechados: ").append(vendas.size()).append("\n\n");

        contexto.append("🧠 INTELIGÊNCIA DE MERCADO E ESTATÍSTICAS:\n");
        contexto.append("- Top Tendência (Mais Vendido): ").append(produtoAltaTendencia).append("\n");
        contexto.append("- Maior Oportunidade (Produto Mais Parado): ").append(produtoParado).append("\n");
        
        contexto.append("- Análise Curva ABC:\n");
        for (Map<String, Object> c : curvaABC) {
            contexto.append("  * ").append(c.get("name")).append(" (").append(c.get("value")).append("% do lucro): ").append(c.get("produtos")).append("\n");
        }

        contexto.append("- Tráfego de Vendas por Horário:\n");
        for (Map<String, Object> t : trafego) {
            contexto.append("  * ").append(t.get("hora")).append(" : ").append(t.get("vendas")).append(" vendas\n");
        }

        if (!previsoes.isEmpty()) {
            contexto.append("- Projeções Futuras (Regressão Linear):\n");
            for (Map<String, Object> p : previsoes) {
                if (p.get("realizado") == null) {
                    contexto.append("  * ").append(p.get("periodo")).append(" (Previsto): R$ ").append(p.get("previsto")).append("\n");
                }
            }
        }

        if (crossSelling != null && !crossSelling.isEmpty()) {
            contexto.append("- Padrões de Cross-Selling:\n");
            for (Map<String, String> c : crossSelling) {
                contexto.append("  * Leva ").append(c.get("principal")).append(" + ").append(c.get("associado")).append(" (Probabilidade: ").append(c.get("probabilidade")).append(")\n");
            }
        }

        contexto.append("\n📦 ESTOQUE GERAL:\n");
        for(Produto p : produtos) {
            contexto.append("- ").append(p.getNome())
                    .append(" | Estoque: ").append(p.getEstoque())
                    .append(" | Preço: R$ ").append(p.getPreco());
            if (p.getPrecoPromocional() != null) {
                contexto.append(" | Promo: R$ ").append(p.getPrecoPromocional());
            }
            contexto.append("\n");
        }

        contexto.append("\nPergunta do usuário: \"").append(perguntaUsuario).append("\"\n");

        // 4. Executa a Pergunta
        String resposta = geminiService.perguntarParaIA(contexto.toString());

        // 5. Interceptador de Agendamentos (Calendário)
        if (resposta.contains("[AGENDAR:")) {
            try {
                String extrato = resposta.substring(resposta.indexOf("[AGENDAR:") + 9, resposta.indexOf("]"));
                String[] partes = extrato.split("\\|");
                Evento novoEvento = new Evento(partes[0].trim(), partes[1].trim(), partes[2].trim(), partes[3].trim());
                eventoRepository.save(novoEvento); 
                resposta = "✅ Feito! Compromisso **'" + partes[0].trim() + "'** agendado no calendário para o dia " + partes[1].trim() + " às " + partes[2].trim() + ".";
            } catch (Exception e) {
                resposta = "❌ Entendi que você quer agendar algo, mas não consegui extrair a data ou hora exata. Tente dizer: 'Agende uma reunião amanhã às 14:00'.";
            }
        }

        return ResponseEntity.ok(Map.of("resposta", resposta));
    }
}