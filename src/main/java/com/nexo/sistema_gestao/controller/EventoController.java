package com.nexo.sistema_gestao.controller;

import com.nexo.sistema_gestao.model.Evento;
import com.nexo.sistema_gestao.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/eventos")
@CrossOrigin(origins = "*")
public class EventoController {

    @Autowired
    private EventoRepository eventoRepository;

    // Buscar todos os eventos
    @GetMapping
    public ResponseEntity<List<Evento>> listarTodos() {
        return ResponseEntity.ok(eventoRepository.findAll());
    }

    // Criar um novo evento
    @PostMapping
    public ResponseEntity<Evento> criarEvento(@RequestBody Evento evento) {
        Evento salvo = eventoRepository.save(evento);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // Editar um evento existente
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarEvento(@PathVariable Long id, @RequestBody Evento detalhesEvento) {
        Optional<Evento> eventoOpt = eventoRepository.findById(id);
        
        if (eventoOpt.isPresent()) {
            Evento evento = eventoOpt.get();
            evento.setTitulo(detalhesEvento.getTitulo());
            evento.setData(detalhesEvento.getData());
            evento.setHora(detalhesEvento.getHora());
            evento.setDescricao(detalhesEvento.getDescricao());
            
            return ResponseEntity.ok(eventoRepository.save(evento));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"erro\": \"Evento não encontrado!\"}");
    }

    // Excluir um evento
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirEvento(@PathVariable Long id) {
        if (eventoRepository.existsById(id)) {
            eventoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}