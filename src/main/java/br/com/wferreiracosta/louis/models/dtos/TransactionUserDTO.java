package br.com.wferreiracosta.louis.models.dtos;

import br.com.wferreiracosta.louis.models.entities.UserEntity;

public record TransactionUserDTO(

        String name,
        String surname,
        String email

) {

    public TransactionUserDTO(UserEntity entity) {
        this(entity.getName(), entity.getSurname(), entity.getEmail());
    }
    
}
