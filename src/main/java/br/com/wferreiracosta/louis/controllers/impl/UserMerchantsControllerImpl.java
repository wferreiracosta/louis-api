package br.com.wferreiracosta.louis.controllers.impl;

import br.com.wferreiracosta.louis.controllers.UserMerchantsController;
import br.com.wferreiracosta.louis.models.dtos.UserDTO;
import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import static br.com.wferreiracosta.louis.models.enums.UserType.MERCHANT;


@RestController
@RequestMapping("/users/merchants")
@RequiredArgsConstructor
public class UserMerchantsControllerImpl implements UserMerchantsController {

    private final UserService service;

    @Override
    public UserEntity save(@Valid @RequestBody final UserDTO dto) {
        return service.save(dto, MERCHANT);
    }

    @Override
    public Page<UserEntity> findPageable(
            @RequestParam(value = "page", defaultValue = "0") final Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "24") final Integer linesPerPage,
            @RequestParam(value = "orderBy", defaultValue = "name") final String orderBy,
            @RequestParam(value = "direction", defaultValue = "ASC") final String direction
    ) {
        return service.findAllPageableByType(page, linesPerPage, orderBy, direction, MERCHANT);
    }

    @Override
    public UserEntity findById(@Valid @PathVariable final Long id) {
        return service.findByTypeAndId(id, MERCHANT);
    }

}
