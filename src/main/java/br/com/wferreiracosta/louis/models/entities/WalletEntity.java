package br.com.wferreiracosta.louis.models.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallets")
@Entity(name = "wallets")
@EqualsAndHashCode(of = "id")
public class WalletEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Schema(description = "User identifier", example = "1")
    private Long id;

    private BigDecimal amount;

    private LocalDateTime createdDate;

    private LocalDateTime updateDate;

    @OneToOne
    @JsonBackReference
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToMany(mappedBy = "transferring")
    private List<TransactionEntity> transferring = new ArrayList<>();

    @OneToMany(mappedBy = "receiving")
    private List<TransactionEntity> receiving = new ArrayList<>();

}

