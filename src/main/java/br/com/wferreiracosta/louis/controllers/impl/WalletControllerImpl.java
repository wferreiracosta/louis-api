package br.com.wferreiracosta.louis.controllers.impl;

import br.com.wferreiracosta.louis.controllers.WalletController;
import br.com.wferreiracosta.louis.models.entities.WalletEntity;
import br.com.wferreiracosta.louis.services.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletControllerImpl implements WalletController {

    private final WalletService service;

    @Override
    public WalletEntity findById(@Valid @PathVariable final Long id) {
        return service.findById(id);
    }

    @Override
    public WalletEntity findByUserId(@Valid @PathVariable final Long id) {
        return service.findByUserId(id);
    }

}
