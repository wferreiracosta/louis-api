package br.com.wferreiracosta.louis.repositories;

import br.com.wferreiracosta.louis.models.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByDocument(String document);

    Optional<UserEntity> findByEmail(String email);

}
