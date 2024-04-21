package br.com.wferreiracosta.louis.services.impl;

import br.com.wferreiracosta.louis.exceptions.ObjectNotFoundException;
import br.com.wferreiracosta.louis.models.dtos.UserDTO;
import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.models.entities.WalletEntity;
import br.com.wferreiracosta.louis.models.enums.UserType;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import br.com.wferreiracosta.louis.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public UserEntity save(final UserDTO dto, final UserType type) {
        final var wallet = WalletEntity.builder()
                .amount(new BigDecimal("0"))
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        final var user = new UserEntity(dto);
        user.setWallet(wallet);
        wallet.setUser(user);

        user.setType(type);
        return repository.save(user);
    }

    @Override
    public Page<UserEntity> findAllPageableByType(
            final Integer page,
            final Integer linesPerPage,
            final String orderBy,
            final String direction,
            final UserType type
    ) {
        final var pageRequest = PageRequest.of(page, linesPerPage, Sort.Direction.valueOf(direction), orderBy);
        return repository.findByType(type, pageRequest);
    }

    public UserEntity findByTypeAndId(final Long id, final UserType userType) {
        return repository.findByIdAndType(id, userType).orElseThrow(
                () -> new ObjectNotFoundException(format("No user found with the Id: %s", id))
        );
    }

    @Override
    public UserEntity findById(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ObjectNotFoundException(format("No user found with the Id: %s", id))
        );
    }

}
