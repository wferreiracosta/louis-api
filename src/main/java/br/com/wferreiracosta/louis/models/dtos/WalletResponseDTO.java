package br.com.wferreiracosta.louis.models.dtos;

import br.com.wferreiracosta.louis.models.entities.WalletEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletResponseDTO(
        Long id,
        BigDecimal amount,
        LocalDateTime createdDate,
        LocalDateTime updateDate
) {
    public static WalletResponseDTO fromEntity(final WalletEntity entity) {
        if (entity == null) {
            return null;
        }
        return new WalletResponseDTO(
                entity.getId(),
                entity.getAmount(),
                entity.getCreatedDate(),
                entity.getUpdateDate()
        );
    }
}
