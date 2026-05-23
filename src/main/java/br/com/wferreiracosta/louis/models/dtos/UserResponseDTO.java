package br.com.wferreiracosta.louis.models.dtos;

import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.models.enums.UserType;

public record UserResponseDTO(
        Long id,
        String name,
        String surname,
        String document,
        String email,
        UserType type,
        WalletResponseDTO wallet
) {
    public static UserResponseDTO fromEntity(final UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new UserResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getSurname(),
                entity.getDocument(),
                entity.getEmail(),
                entity.getType(),
                WalletResponseDTO.fromEntity(entity.getWallet())
        );
    }
}
