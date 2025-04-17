package br.com.wferreiracosta.louis.models.entities;

import br.com.wferreiracosta.louis.models.enums.UserType;
import br.com.wferreiracosta.louis.models.parameters.UserParameter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Entity(name = "users")
@EqualsAndHashCode(of = "id")
public class UserEntity {

    public UserEntity(final UserParameter parameter) {
        this.name = parameter.name();
        this.surname = parameter.surname();
        this.document = parameter.document();
        this.email = parameter.email();
        this.password = parameter.password();
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Schema(description = "User identifier", example = "1")
    private Long id;

    @Schema(description = "Username", example = "Pedro")
    private String name;

    @Schema(description = "Surname", example = "Silva")
    private String surname;

    @Column(unique = true)
    @Schema(description = "User document", example = "12345678996")
    private String document;

    @Column(unique = true)
    @Schema(description = "User email", example = "pedro@mail.com")
    private String email;

    @JsonIgnore
    private String password;

    @Schema(description = "User type", example = "COMMON")
    @Enumerated(EnumType.STRING)
    private UserType type;

    @JsonManagedReference
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private WalletEntity wallet;

}
