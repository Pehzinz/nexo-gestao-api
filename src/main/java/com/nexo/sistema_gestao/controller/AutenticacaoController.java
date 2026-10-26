package com.nexo.sistema_gestao.controller;

import com.nexo.sistema_gestao.dto.LoginRequestDTO;
import com.nexo.sistema_gestao.model.Usuario;
import com.nexo.sistema_gestao.repository.UsuarioRepository;
import com.nexo.sistema_gestao.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/login")
@CrossOrigin(origins = "*")
public class AutenticacaoController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService; //Injeta o gerador de Tokens

    @PostMapping
    public ResponseEntity<?> realizarLogin(@RequestBody LoginRequestDTO dadosLogin) {
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCpf(dadosLogin.cpf());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"erro\": \"Usuário não encontrado.\"}");
        }

        Usuario usuario = usuarioOpt.get();

        // O matches() compara a senha digitada com a criptografada
        if (passwordEncoder.matches(dadosLogin.senha(), usuario.getSenha())) {
            
            // Gera o token JWT para este usuário
            String token = tokenService.gerarToken(usuario);
            
            // CORREÇÃO: Pacote JSON completo com o Nome!
            Map<String, String> resposta = new HashMap<>();
            resposta.put("token", token);
            resposta.put("perfil", usuario.getPerfil().toString());
            resposta.put("nomeCompleto", usuario.getNomeCompleto()); 
            
            return ResponseEntity.ok(resposta);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"erro\": \"Senha incorreta.\"}");
        }
    }
}