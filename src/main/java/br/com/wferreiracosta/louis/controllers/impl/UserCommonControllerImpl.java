package br.com.wferreiracosta.louis.controllers.impl;

import br.com.wferreiracosta.louis.controllers.UserCommonController;
import br.com.wferreiracosta.louis.models.dtos.UserDTO;
import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static br.com.wferreiracosta.louis.models.enums.UserType.COMMON;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/common")
public class UserCommonControllerImpl implements UserCommonController {

    private final UserService service;

    @Override
    public UserEntity save(@Valid @RequestBody UserDTO dto) {
        return service.save(dto, COMMON);
    }

    @Override
    public Page<UserEntity> findPageable(
            @RequestParam(value = "page", defaultValue = "0") final Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "24") final Integer linesPerPage,
            @RequestParam(value = "orderBy", defaultValue = "name") final String orderBy,
            @RequestParam(value = "direction", defaultValue = "ASC") final String direction
    ) {
        return service.findAllPageableByType(page, linesPerPage, orderBy, direction, COMMON);
    }

}
