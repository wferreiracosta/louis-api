# 013 — Autenticação e Autorização com JWT

**Sprint:** 🔵 4 — Segurança Completa  
**Categoria:** Segurança  
**Impacto:** 🔴 Crítico  
**Esforço:** ⏱️ Alto

---

## Problema

Nenhuma rota da API possui mecanismo de autenticação. Qualquer cliente pode:

- Cadastrar usuários (`POST /users/common`, `POST /users/merchants`)
- Consultar carteiras (`GET /wallets/{id}`)
- Realizar transferências (`POST /transaction`) **em nome de qualquer usuário**

Este é o risco de segurança mais grave da aplicação: um usuário mal-intencionado pode drenar a carteira de outro usuário sem qualquer autenticação.

> **Pré-requisito:** Este plano depende do plano [010](./010-hash-senhas-bcrypt.md) (BCrypt) estar implementado.

---

## Solução

Implementar autenticação stateless via **JWT (JSON Web Token)** com Spring Security.

### Componentes a criar

#### 1. Endpoint de Login

```
POST /auth/login
Body: { "email": "user@example.com", "password": "secret" }
Response: { "token": "eyJ...", "expiresIn": 3600 }
```

#### 2. Entidade/Record de Credenciais

```java
public record LoginRequest(String email, String password) {}
public record LoginResponse(String token, long expiresIn) {}
```

#### 3. `JwtService`

Responsável por gerar e validar tokens JWT:

```java
@Service
public class JwtService {
    private final String secretKey; // externalizar via Secrets Manager
    private final long expirationMs = 3_600_000; // 1 hora

    public String generateToken(UserDetails userDetails) { ... }
    public String extractEmail(String token) { ... }
    public boolean isTokenValid(String token, UserDetails userDetails) { ... }
}
```

#### 4. `JwtAuthenticationFilter`

Filtro que intercepta requisições, extrai o token do header `Authorization: Bearer <token>` e autentica o usuário no `SecurityContextHolder`.

#### 5. `SecurityConfig` (atualizada)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/users/common", "/users/merchants").permitAll() // cadastro público
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### Regras de Autorização

Além da autenticação, aplicar autorização por recurso:

| Rota | Regra |
|------|-------|
| `GET /wallets/{id}` | Apenas o dono da carteira |
| `POST /transaction` | Apenas o usuário autenticado como `payer` |
| `GET /users/common` | Apenas o próprio usuário ou admin |

---

## Dependências

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

---

## Verificação

- [ ] `POST /auth/login` retorna token JWT com credenciais válidas
- [ ] `POST /auth/login` retorna HTTP 401 com credenciais inválidas
- [ ] `POST /transaction` sem token retorna HTTP 401
- [ ] `POST /transaction` com token de outro usuário retorna HTTP 403
- [ ] `GET /wallets/{id}` retorna HTTP 403 para usuário autenticado tentando ver carteira alheia
- [ ] Documentação Swagger (`/swagger-ui`) ainda acessível sem autenticação
- [ ] Executar todos os testes (`./mvnw test`) — testes de integração precisarão usar tokens de teste
