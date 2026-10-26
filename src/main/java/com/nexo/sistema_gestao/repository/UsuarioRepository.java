package com.nexo.sistema_gestao.repository;
import com.nexo.sistema_gestao.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository 
public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
    //Ensinando o Spring a buscar automaticamente o usuario especifico pelo CPF;
    Optional<Usuario> findByCpf(String cpf);
} 