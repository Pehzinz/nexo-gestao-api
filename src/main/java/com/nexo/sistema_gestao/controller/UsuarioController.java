package com.nexo.sistema_gestao.controller;

import com.nexo.sistema_gestao.model.Usuario;
import com.nexo.sistema_gestao.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. Devolve a lista de todos os colaboradores registados
    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return ResponseEntity.ok(usuarios);
    }

    // 2. Recebe os dados do React e guarda um novo colaborador
    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Usuario novoUsuario) {
        
        // Verifica se o CPF já existe na base de dados para evitar duplicados
        if (usuarioRepository.findByCpf(novoUsuario.getCpf()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"erro\": \"CPF já cadastrado.\"}");
        }
        
        // Encripta a palavra-passe antes de a guardar, garantindo a segurança
        novoUsuario.setSenha(passwordEncoder.encode(novoUsuario.getSenha()));
        
        usuarioRepository.save(novoUsuario);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }

    // 3. Remove um colaborador através do seu ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> remover(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        
        if (usuarioOpt.isPresent()) {
            usuarioRepository.delete(usuarioOpt.get());
            return ResponseEntity.ok().build();
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // Endpoint para Editar um Colaborador
    @PutMapping("/{id}")
    public ResponseEntity<?> editarColaborador(@PathVariable Long id, @RequestBody Usuario dadosAtualizados) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"erro\": \"Colaborador não encontrado.\"}");
        }

        Usuario usuario = usuarioOpt.get();

        // Atualiza os dados permitidos (NÃO ATUALIZA O CPF)
        usuario.setNomeCompleto(dadosAtualizados.getNomeCompleto());
        usuario.setDataNascimento(dadosAtualizados.getDataNascimento());
        usuario.setTelefone(dadosAtualizados.getTelefone());
        usuario.setEndereco(dadosAtualizados.getEndereco());

        // Se a senha foi enviada no formulário de edição, criptografa e atualiza
        if (dadosAtualizados.getSenha() != null && !dadosAtualizados.getSenha().trim().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(dadosAtualizados.getSenha()));
        }

        usuarioRepository.save(usuario);

        return ResponseEntity.ok("{\"mensagem\": \"Colaborador atualizado com sucesso!\"}");
    }
}