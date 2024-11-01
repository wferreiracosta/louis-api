package br.com.wferreiracosta.louis.repositories;

import br.com.wferreiracosta.louis.models.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRespository extends JpaRepository<TransactionEntity, Long> {
}
