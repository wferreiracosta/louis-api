package br.com.wferreiracosta.louis.services;

import br.com.wferreiracosta.louis.models.dtos.UserDTO;
import br.com.wferreiracosta.louis.models.entities.UserEntity;

public interface UserService {

    UserEntity saveMerchant(UserDTO dto);

}
