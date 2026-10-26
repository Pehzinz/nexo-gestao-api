package com.nexo.sistema_gestao.security;

import com.nexo.sistema_gestao.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository repository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Pega o token do cabeçalho da requisição
        var tokenJWT = recuperarToken(request);

        if (tokenJWT != null) {
            // 2. Descriptografa o token e descobre de qual CPF ele é
            var subject = tokenService.validarToken(tokenJWT);
            var usuarioOpt = repository.findByCpf(subject);

            if (usuarioOpt.isPresent()) {
                var usuario = usuarioOpt.get();
                // 3. Avisa ao Spring: "Pode deixar passar, eu conheço esse usuário e o token é válido!"
                var authentication = new UsernamePasswordAuthenticationToken(usuario, null, null); 
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 4. Continua o fluxo (se não tiver token válido, o Spring bloqueia mais à frente)
        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null) {
            // O padrão da internet é enviar o token com a palavra "Bearer " antes dele. Aqui nós a removemos.
            return authorizationHeader.replace("Bearer ", "");
        }
        return null;
    }
}