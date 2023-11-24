package br.com.wferreiracosta.louis.services;

import br.com.wferreiracosta.louis.models.dtos.UserDTO;
import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.models.enums.UserType;

public interface UserService {

    UserEntity save(UserDTO dto, UserType type);

}
