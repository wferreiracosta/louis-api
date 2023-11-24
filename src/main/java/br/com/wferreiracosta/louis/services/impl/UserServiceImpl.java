package br.com.wferreiracosta.louis.services.impl;

import br.com.wferreiracosta.louis.exceptions.DataViolationException;
import br.com.wferreiracosta.louis.models.dtos.UserDTO;
import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.models.enums.UserType;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import br.com.wferreiracosta.louis.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public UserEntity save(final UserDTO dto, final UserType type) {
        final var entity = new UserEntity(dto);
        entity.setType(type);

        if(repository.findByDocument(dto.document()).isPresent()){
            throw new DataViolationException("There is already a user registered with this document.");
        }

        if(repository.findByEmail(dto.email()).isPresent()){
            throw new DataViolationException("There is already a user registered with this email.");
        }

        return repository.save(entity);
    }

}
