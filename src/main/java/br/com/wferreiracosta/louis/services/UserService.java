package br.com.wferreiracosta.louis.services;

import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.models.enums.UserType;
import br.com.wferreiracosta.louis.models.parameters.UserParameter;
import org.springframework.data.domain.Page;

public interface UserService {

    UserEntity save(UserParameter parameter, UserType type);

    Page<UserEntity> findAllPageableByType(
            Integer page,
            Integer linesPerPage,
            String orderBy,
            String direction,
            UserType type
    );

    UserEntity findByTypeAndId(Long id, UserType userType);

    UserEntity findById(Long id);

}
