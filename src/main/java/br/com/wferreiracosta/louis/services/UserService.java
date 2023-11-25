package br.com.wferreiracosta.louis.services;

import br.com.wferreiracosta.louis.models.dtos.UserDTO;
import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.models.enums.UserType;
import org.springframework.data.domain.Page;

public interface UserService {

    UserEntity save(UserDTO dto, UserType type);

    Page<UserEntity> findAllPageableByType(
            Integer page,
            Integer linesPerPage,
            String orderBy,
            String direction,
            UserType type
    );

}
