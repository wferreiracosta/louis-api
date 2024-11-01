package br.com.wferreiracosta.louis.models.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
@Entity(name = "transactions")
@EqualsAndHashCode(of = "id")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Schema(description = "Transaction identifier", example = "1")
    private Long id;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "transferring_id")
    private WalletEntity transferring;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "receiving_id")
    private WalletEntity receiving;

    private LocalDateTime timestamp;

}
