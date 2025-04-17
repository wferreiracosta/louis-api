package br.com.wferreiracosta.louis.repositories;

import br.com.wferreiracosta.louis.models.entities.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<WalletEntity, Long> {
}
