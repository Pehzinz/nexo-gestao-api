package com.nexo.sistema_gestao.service;

import com.nexo.sistema_gestao.model.Evento;
import com.nexo.sistema_gestao.model.Produto;
import com.nexo.sistema_gestao.model.Usuario;
import com.nexo.sistema_gestao.model.Venda;
import com.nexo.sistema_gestao.repository.EventoRepository;
import com.nexo.sistema_gestao.repository.ProdutoRepository;
import com.nexo.sistema_gestao.repository.UsuarioRepository;
import com.nexo.sistema_gestao.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TelegramService extends TelegramLongPollingBot {

    private static final String BOT_TOKEN = "BOT_NO_SERVIDOR"; 
    private static final String BOT_USERNAME = "NexoGestao_bot"; 
    private static final String CHAT_ID = "8806880654"; 

    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private VendaRepository vendaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EventoRepository eventoRepository;
    
    @Autowired private GeminiService geminiService;
    @Autowired private EstatisticaService estatisticaService;

    public TelegramService() { super(BOT_TOKEN); } 
    @Override public String getBotUsername() { return BOT_USERNAME; }
    @Override public String getBotToken() { return BOT_TOKEN; }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String mensagemRecebida = update.getMessage().getText().trim().toLowerCase();
            String chatIdUsuario = update.getMessage().getChatId().toString();

            System.out.println("📩 Comando/Mensagem Telegram: " + mensagemRecebida);

            if (mensagemRecebida.equals("/estoque")) { responderEstoque(chatIdUsuario); } 
            else if (mensagemRecebida.equals("/vendas")) { responderVendas(chatIdUsuario); } 
            else if (mensagemRecebida.equals("/equipe") || mensagemRecebida.equals("/usuarios") || mensagemRecebida.equals("/colaboradores") || mensagemRecebida.equals("/funcionarios")) { responderEquipe(chatIdUsuario); } 
            else if (mensagemRecebida.equals("/resumo")) { responderResumo(chatIdUsuario); } 
            else if (mensagemRecebida.equals("/insights") || mensagemRecebida.equals("/tendencias")) { responderInsights(chatIdUsuario); }
            else if (mensagemRecebida.equals("/alertas") || mensagemRecebida.equals("/ruptura")) { responderAlertas(chatIdUsuario); }
            else if (mensagemRecebida.equals("/agenda") || mensagemRecebida.equals("/compromissos")) { responderAgenda(chatIdUsuario); } // NOVO
            else if (mensagemRecebida.equals("/promocoes") || mensagemRecebida.equals("/ofertas")) { responderPromocoes(chatIdUsuario); } // NOVO
            else if (mensagemRecebida.equals("/ajuda") || mensagemRecebida.equals("/start")) { enviarMenuAjuda(chatIdUsuario); } 
            else { 
                // A INTELIGÊNCIA ARTIFICIAL VOLTA À AÇÃO AQUI
                responderComIA(chatIdUsuario, update.getMessage().getText()); 
            }
        }
    }

    // --- 1. COMANDO /RESUMO ---
    private void responderResumo(String chatId) {
        String resposta = "📈 *Resumo Gerencial Nexo:*\n\n" +
                          "📦 Produtos Cadastrados: " + produtoRepository.count() + "\n" +
                          "🛒 Vendas Realizadas: " + vendaRepository.count() + "\n" +
                          "👥 Membros da Equipe: " + usuarioRepository.count() + "\n" +
                          "📅 Eventos na Agenda: " + eventoRepository.count() + "\n\n" +
                          "_(Use o menu /ajuda para ver relatórios detalhados)_";
        enviarMensagem(chatId, resposta);
    }

    // --- 2. COMANDO /VENDAS ---
    private void responderVendas(String chatId) {
        List<Venda> vendas = vendaRepository.findAll();
        if (vendas.isEmpty()) { enviarMensagem(chatId, "📊 Nenhuma venda foi registrada até o momento."); return; }

        BigDecimal faturamentoTotal = BigDecimal.ZERO;
        for (Venda v : vendas) {
            if (v.getTotal() != null) faturamentoTotal = faturamentoTotal.add(v.getTotal());
        }

        String resposta = "📊 *Relatório Financeiro:*\n\n" +
                          "🛒 Total de Pedidos: " + vendas.size() + "\n" +
                          "💰 Faturamento Bruto: R$ " + String.format("%.2f", faturamentoTotal.doubleValue()) + "\n" +
                          "💵 Ticket Médio: R$ " + String.format("%.2f", (faturamentoTotal.doubleValue() / vendas.size())) + "\n";
        enviarMensagem(chatId, resposta);
    }

    // --- 3. COMANDO /ESTOQUE (MELHORADO COM CAPITAL IMOBILIZADO) ---
    private void responderEstoque(String chatId) {
        List<Produto> produtos = produtoRepository.findAll();
        if (produtos.isEmpty()) { enviarMensagem(chatId, "📦 Seu estoque está vazio."); return; }

        BigDecimal capitalImobilizado = BigDecimal.ZERO;
        StringBuilder resposta = new StringBuilder("📦 *Inventário de Produtos:*\n\n");
        
        for (Produto p : produtos) {
            if(p.isAtivo()) {
                capitalImobilizado = capitalImobilizado.add(BigDecimal.valueOf(p.getPreco()).multiply(BigDecimal.valueOf(p.getEstoque())));
                resposta.append("▪️ *").append(p.getNome()).append("*\n").append("    Estoque: ").append(p.getEstoque()).append(" un. | Preço: R$ ").append(String.format("%.2f", p.getPreco())).append("\n\n");
            }
        }
        resposta.append("💎 *Capital Total no Estoque:* R$ ").append(String.format("%.2f", capitalImobilizado.doubleValue()));
        enviarMensagem(chatId, resposta.toString());
    }

    // --- 4. COMANDO /ALERTAS ---
    private void responderAlertas(String chatId) {
        List<Produto> produtos = produtoRepository.findAll();
        List<Produto> estoqueBaixo = produtos.stream().filter(p -> p.getEstoque() <= 5 && p.isAtivo()).collect(Collectors.toList());

        if (estoqueBaixo.isEmpty()) { enviarMensagem(chatId, "✅ *Estoque Saudável!*\nNenhum produto apresenta risco de ruptura."); return; }

        StringBuilder resposta = new StringBuilder("⚠️ *Alertas de Ruptura de Estoque:*\n\n");
        for (Produto p : estoqueBaixo) {
            String icone = p.getEstoque() == 0 ? "🚨" : "⚠️";
            resposta.append(icone).append(" *").append(p.getNome()).append("*\n    Restam: ").append(p.getEstoque()).append(" un.\n\n");
        }
        enviarMensagem(chatId, resposta.toString());
    }

    // --- 5. COMANDO /INSIGHTS ---
    private void responderInsights(String chatId) {
        enviarMensagem(chatId, "⏳ Gerando inteligência de mercado...");
        Map<String, Object> curvaEFluxo = estatisticaService.calcularCurvaABCFluxo();
        @SuppressWarnings("unchecked") List<Map<String, Object>> curvaABC = (List<Map<String, Object>>) curvaEFluxo.get("curva");
        
        Map<String, Object> trafegoData = estatisticaService.calcularTrafego();
        @SuppressWarnings("unchecked") List<Map<String, Object>> trafego = (List<Map<String, Object>>) trafegoData.get("grafico");
        
        String horaGelo = "Indefinida";
        int minVendas = Integer.MAX_VALUE;
        for (Map<String, Object> t : trafego) {
            int vendas = (Integer) t.get("vendas");
            if (vendas < minVendas && vendas > 0) { minVendas = vendas; horaGelo = (String) t.get("hora"); }
        }

        StringBuilder resposta = new StringBuilder("🧠 *Centro de Inteligência de Mercado*\n\n");
        resposta.append("🚀 *Top Tendência:*\n").append(curvaEFluxo.get("produtoAltaTendencia")).append("\n\n");
        resposta.append("📉 *Oportunidade (Curva C):*\n").append(curvaEFluxo.get("produtoParado")).append("\n\n");
        resposta.append("❄️ *Hora Gelo (Menor Movimento):*\nPor volta das ").append(horaGelo).append("\n\n");
        
        resposta.append("📊 *Resumo da Matriz de Lucro (Curva ABC):*\n");
        for (Map<String, Object> c : curvaABC) {
            resposta.append("▪️ *").append(c.get("name")).append("*: Responsável por ").append(c.get("value")).append("% do lucro.\n");
        }
        enviarMensagem(chatId, resposta.toString());
    }

    // --- 6. COMANDO /AGENDA (NOVO MÓDULO) ---
    private void responderAgenda(String chatId) {
        List<Evento> eventos = eventoRepository.findAll();
        String hojeStr = LocalDate.now().toString();
        
        List<Evento> eventosFuturos = eventos.stream()
            .filter(e -> e.getData().compareTo(hojeStr) >= 0)
            .sorted((e1, e2) -> e1.getData().compareTo(e2.getData()))
            .collect(Collectors.toList());

        if (eventosFuturos.isEmpty()) { enviarMensagem(chatId, "📅 *Sua Agenda está livre!*\nNenhum compromisso marcado para os próximos dias."); return; }

        StringBuilder resposta = new StringBuilder("📅 *Próximos Compromissos:*\n\n");
        for (Evento e : eventosFuturos) {
            resposta.append("📌 *").append(e.getTitulo()).append("*\n")
                    .append("    Data: ").append(e.getData()).append(" às ").append(e.getHora()).append("\n")
                    .append("    Detalhe: ").append(e.getDescricao() != null && !e.getDescricao().isEmpty() ? e.getDescricao() : "Sem descrição adicionada.").append("\n\n");
        }
        enviarMensagem(chatId, resposta.toString());
    }

    // --- 7. COMANDO /PROMOCOES (NOVO MÓDULO) ---
    private void responderPromocoes(String chatId) {
        List<Produto> produtos = produtoRepository.findAll();
        List<Produto> emOferta = produtos.stream()
            .filter(p -> p.isAtivo() && p.getPrecoPromocional() != null)
            .collect(Collectors.toList());

        if (emOferta.isEmpty()) { enviarMensagem(chatId, "🏷️ *Nenhuma promoção ativa no momento.*"); return; }

        StringBuilder resposta = new StringBuilder("🏷️ *Ofertas Ativas na Loja:*\n\n");
        for (Produto p : emOferta) {
            resposta.append("▪️ *").append(p.getNome()).append("*\n")
                    .append("    De: R$ ").append(String.format("%.2f", p.getPreco())).append("\n")
                    .append("    Por: *R$ ").append(String.format("%.2f", p.getPrecoPromocional())).append("*\n\n");
        }
        enviarMensagem(chatId, resposta.toString());
    }

    // --- 8. COMANDO /EQUIPE ---
    private void responderEquipe(String chatId) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        if (usuarios.isEmpty()) { enviarMensagem(chatId, "👥 Nenhum colaborador cadastrado."); return; }
        StringBuilder resposta = new StringBuilder("👥 *Quadro de Colaboradores:*\n\n");
        for (Usuario u : usuarios) { resposta.append("👤 *").append(u.getNomeCompleto()).append("*\n   Acesso: ").append(u.getPerfil()).append("\n\n"); }
        enviarMensagem(chatId, resposta.toString());
    }

    // --- 9. MENU DE AJUDA ---
    private void enviarMenuAjuda(String chatId) {
        String texto = "🤖 *Bem-vindo ao Terminal e Assistente Nexo*\n\n" +
                       "Você pode conversar livremente comigo para pedir conselhos, análises ou para *agendar eventos*.\n\n" +
                       "Ou use os comandos rápidos abaixo:\n\n" +
                       "📊 *Métricas e Inteligência*\n" +
                       "/resumo - Visão geral do sistema\n" +
                       "/vendas - Faturamento e pedidos\n" +
                       "/insights - Análise de Curva ABC e tendências\n\n" +
                       "📦 *Controle Operacional*\n" +
                       "/estoque - Catálogo e Capital Imobilizado\n" +
                       "/alertas - Produtos perto de esgotar\n" +
                       "/promocoes - Ver descontos ativos\n\n" +
                       "📅 *Organização*\n" +
                       "/agenda - Próximos compromissos marcados\n" +
                       "/colaboradores - Quadro de funcionários";
        enviarMensagem(chatId, texto);
    }

    public void enviarMensagem(String chatId, String texto) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(texto);
        message.setParseMode("Markdown"); 
        try { execute(message); } catch (TelegramApiException e) { System.err.println("❌ Falha ao responder no Telegram: " + e.getMessage()); }
    }

    public void enviarAlerta(String mensagem) { enviarMensagem(CHAT_ID, mensagem); }

    // --- A INTELIGÊNCIA ARTIFICIAL ATIVA ---
    private void responderComIA(String chatId, String perguntaUsuario) {
        enviarMensagem(chatId, "⏳ Analisando a sua solicitação...");

        List<Produto> produtos = produtoRepository.findAll();
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Venda> vendas = vendaRepository.findAll();
        BigDecimal faturamento = BigDecimal.ZERO;
        for (Venda v : vendas) { if (v.getTotal() != null) faturamento = faturamento.add(v.getTotal()); }

        Map<String, Object> curvaEFluxo = estatisticaService.calcularCurvaABCFluxo();
        @SuppressWarnings("unchecked") List<Map<String, Object>> curvaABC = (List<Map<String, Object>>) curvaEFluxo.get("curva");
        String produtoAltaTendencia = (String) curvaEFluxo.get("produtoAltaTendencia");
        String produtoParado = (String) curvaEFluxo.get("produtoParado");

        StringBuilder contexto = new StringBuilder();
        contexto.append("Você é o Consultor Estratégico do 'Nexo Gestão'.\n");
        contexto.append("REGRA DE CALENDÁRIO: Se o usuário pedir para agendar, responda EXATAMENTE neste formato: [AGENDAR: Titulo | AAAA-MM-DD | HH:MM | Descricao breve]\n\n");
        contexto.append("📊 DADOS FINANCEIROS:\n- Faturamento: R$ ").append(String.format("%.2f", faturamento.doubleValue())).append("\n- Pedidos: ").append(vendas.size()).append("\n\n");
        
        contexto.append("🧠 INTELIGÊNCIA:\n- Top Tendência: ").append(produtoAltaTendencia).append("\n- Oportunidade: ").append(produtoParado).append("\n- Curva ABC:\n");
        for (Map<String, Object> c : curvaABC) { contexto.append("  * ").append(c.get("name")).append(" (").append(c.get("value")).append("% do lucro): ").append(c.get("produtos")).append("\n"); }

        contexto.append("\n📦 ESTOQUE:\n");
        for(Produto p : produtos) {
            contexto.append("- [").append(p.getId()).append("] ").append(p.getNome()).append(" | Estoque: ").append(p.getEstoque()).append(" un. | R$").append(p.getPreco()).append("\n");
        }

        contexto.append("\nPergunta do usuário: \"").append(perguntaUsuario).append("\"\n");

        String respostaInteligente = geminiService.perguntarParaIA(contexto.toString());

        if (respostaInteligente.contains("[AGENDAR:")) {
            try {
                String extrato = respostaInteligente.substring(respostaInteligente.indexOf("[AGENDAR:") + 9, respostaInteligente.indexOf("]"));
                String[] partes = extrato.split("\\|");
                Evento novoEvento = new Evento(partes[0].trim(), partes[1].trim(), partes[2].trim(), partes[3].trim());
                eventoRepository.save(novoEvento);
                respostaInteligente = "✅ Feito via Telegram! Agendei **'" + partes[0].trim() + "'** para o dia " + partes[1].trim() + " às " + partes[2].trim() + ". Acesse a Web ou digite /agenda para conferir.";
            } catch (Exception e) {
                respostaInteligente = "❌ Entendi que você quer agendar algo, mas faltaram informações. Diga a data e a hora exata.";
            }
        }

        enviarMensagem(chatId, respostaInteligente);
    }
}