package com.nexo.sistema_gestao.repository;

import com.nexo.sistema_gestao.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    // Método bônus para futuramente buscarmos eventos a partir de uma data específica
    List<Evento> findByDataGreaterThanEqualOrderByDataAscHoraAsc(String data);
}