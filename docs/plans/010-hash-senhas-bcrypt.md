# 010 — Hash de Senhas com BCrypt

**Sprint:** 🟡 3 — Preparação para Produção  
**Categoria:** Segurança  
**Impacto:** 🔴 Crítico  
**Esforço:** ⏱️ Médio

---

## Problema

Em [`UserEntity.java`](../../src/main/java/br/com/wferreiracosta/louis/models/entities/UserEntity.java), a senha é persistida diretamente como texto puro:

```java
public UserEntity(final UserParameter parameter) {
    // ...
    this.password = parameter.password(); // plain text
}
```

Isso viola:
- **LGPD** (Art. 46) — obrigação de adotar medidas de segurança para proteção de dados pessoais
- **OWASP Top 10** — A02:2021 Cryptographic Failures
- **Boas práticas de segurança** — senhas nunca devem ser armazenadas em texto claro

Em caso de vazamento do banco de dados, todas as senhas dos usuários ficam imediatamente expostas.

---

## Solução

### 1. Adicionar dependência do Spring Security ao `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

> **Atenção:** Adicionar `spring-boot-starter-security` habilita autenticação básica por padrão em todos os endpoints. Para não bloquear o sistema antes da implementação completa do JWT (plano [013](./013-autenticacao-jwt.md)), criar uma configuração de segurança temporária que permita todas as requisições:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 2. Injetar `PasswordEncoder` em `UserServiceImpl`

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final WalletService walletService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserEntity save(final UserParameter parameter, final UserType type) {
        // ...
        final var encodedPassword = passwordEncoder.encode(parameter.password());

        final var user = UserEntity.builder()
                .name(parameter.name())
                .surname(parameter.surname())
                .document(parameter.document())
                .email(parameter.email())
                .password(encodedPassword) // hash, não plain text
                .type(type)
                .build();

        // ...
    }
}
```

### 3. Remover o construtor de mapeamento de `UserEntity`

O construtor `UserEntity(UserParameter)` deve ser removido ou alterado para não aceitar mais a senha em plain text, garantindo que o encoding sempre ocorra na camada de serviço.

---

## Verificação

- [ ] Cadastrar um usuário e verificar que a senha no banco começa com `$2a$` (formato BCrypt)
- [ ] Confirmar que todos os endpoints continuam acessíveis (security temporariamente aberta)
- [ ] Executar todos os testes (`./mvnw test`)
- [ ] Garantir que não há nenhuma senha em texto plano em logs, respostas de API ou banco de dados
