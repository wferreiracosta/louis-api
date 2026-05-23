package br.com.wferreiracosta.louis.repositories;

import br.com.wferreiracosta.louis.models.entities.WalletEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<WalletEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM wallets w WHERE w.user.id = :userId")
    Optional<WalletEntity> findByUserIdWithLock(@Param("userId") Long userId);

}
