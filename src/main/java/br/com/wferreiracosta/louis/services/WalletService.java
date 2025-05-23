package br.com.wferreiracosta.louis.services;

import br.com.wferreiracosta.louis.models.entities.WalletEntity;

public interface WalletService {

    WalletEntity findById(Long id);

    WalletEntity findByUserId(Long id);

    WalletEntity update(WalletEntity entity);

}
