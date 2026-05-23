package br.com.wferreiracosta.louis.controllers.impl;

import br.com.wferreiracosta.louis.controllers.UserCommonController;
import br.com.wferreiracosta.louis.models.dtos.UserResponseDTO;
import br.com.wferreiracosta.louis.models.parameters.UserParameter;
import br.com.wferreiracosta.louis.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import static br.com.wferreiracosta.louis.models.enums.UserType.COMMON;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/common")
public class UserCommonControllerImpl implements UserCommonController {

    private final UserService service;

    @Override
    public UserResponseDTO save(@Valid @RequestBody UserParameter parameter) {
        return UserResponseDTO.fromEntity(service.save(parameter, COMMON));
    }

    @Override
    public Page<UserResponseDTO> findPageable(
            @RequestParam(value = "page", defaultValue = "0") final Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "24") final Integer linesPerPage,
            @RequestParam(value = "orderBy", defaultValue = "name") final String orderBy,
            @RequestParam(value = "direction", defaultValue = "ASC") final String direction
    ) {
        return service.findAllPageableByType(page, linesPerPage, orderBy, direction, COMMON)
                .map(UserResponseDTO::fromEntity);
    }

    @Override
    public UserResponseDTO findById(@PathVariable Long id) {
        return UserResponseDTO.fromEntity(service.findByTypeAndId(id, COMMON));
    }

}
