package br.com.wferreiracosta.louis.controllers.impl;

import br.com.wferreiracosta.louis.controllers.UserController;
import br.com.wferreiracosta.louis.models.dtos.UserDTO;
import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static br.com.wferreiracosta.louis.models.enums.UserType.COMMON;
import static br.com.wferreiracosta.louis.models.enums.UserType.MERCHANT;


@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserControllerImpl implements UserController {

    private final UserService service;

    @Override
    public UserEntity saveMerchant(@Valid @RequestBody final UserDTO dto) {
        return service.save(dto, MERCHANT);
    }

    @Override
    public UserEntity saveCommon(@Valid @RequestBody UserDTO dto) {
        return service.save(dto, COMMON);
    }

}
