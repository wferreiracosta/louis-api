package br.com.wferreiracosta.louis.controllers.impl;

import br.com.wferreiracosta.louis.controllers.UserMerchantsController;
import br.com.wferreiracosta.louis.models.dtos.UserResponseDTO;
import br.com.wferreiracosta.louis.models.parameters.UserParameter;
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
    public UserResponseDTO save(@Valid @RequestBody final UserParameter parameter) {
        return UserResponseDTO.fromEntity(service.save(parameter, MERCHANT));
    }

    @Override
    public Page<UserResponseDTO> findPageable(
            @RequestParam(value = "page", defaultValue = "0") final Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "24") final Integer linesPerPage,
            @RequestParam(value = "orderBy", defaultValue = "name") final String orderBy,
            @RequestParam(value = "direction", defaultValue = "ASC") final String direction
    ) {
        return service.findAllPageableByType(page, linesPerPage, orderBy, direction, MERCHANT)
                .map(UserResponseDTO::fromEntity);
    }

    @Override
    public UserResponseDTO findById(@Valid @PathVariable final Long id) {
        return UserResponseDTO.fromEntity(service.findByTypeAndId(id, MERCHANT));
    }

}
