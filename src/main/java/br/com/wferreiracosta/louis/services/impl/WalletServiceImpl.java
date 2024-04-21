package br.com.wferreiracosta.louis.services.impl;

import br.com.wferreiracosta.louis.exceptions.ObjectNotFoundException;
import br.com.wferreiracosta.louis.models.entities.WalletEntity;
import br.com.wferreiracosta.louis.repositories.WalletRepository;
import br.com.wferreiracosta.louis.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository repository;

    @Override
    public WalletEntity findById(final Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ObjectNotFoundException(format("No wallet found with the Id: %s", id))
        );
    }

}
