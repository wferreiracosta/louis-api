package br.com.wferreiracosta.louis.repositories;

import br.com.wferreiracosta.louis.models.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
