# 008 — Substituir Gson pelo Jackson (`ObjectMapper`)

**Sprint:** 🟠 2 — Limpeza Técnica  
**Categoria:** Refatoração / Dependências  
**Impacto:** 🟢 Baixo  
**Esforço:** ⏱️ Baixo

---

## Problema

O [`pom.xml`](../../pom.xml) declara a dependência do Google Gson:

```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

Ela é utilizada em apenas um local: [`SecretsManagerConfig.java`](../../src/main/java/br/com/wferreiracosta/louis/configs/SecretsManagerConfig.java), para desserializar o JSON retornado pelo AWS Secrets Manager:

```java
final var gson = new Gson();
return gson.fromJson(value.get(), SecretsManagerProperties.class);
```

O Jackson (`ObjectMapper`) já está disponível no classpath via `spring-boot-starter-web`, tornando o Gson uma **dependência redundante** que:
- Aumenta o tamanho do artefato JAR
- Expõe superfície adicional a CVEs
- Adiciona inconsistência (dois parsers JSON no mesmo projeto)

---

## Solução

### 1. Remover dependência do `pom.xml`

```xml
<!-- Remover este bloco -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

### 2. Injetar `ObjectMapper` em `SecretsManagerConfig`

```java
@Slf4j
@Profile("!test")
@Configuration
@RequiredArgsConstructor
public class SecretsManagerConfig {

    private final AwsProperties awsProperties;
    private final ObjectMapper objectMapper; // injetado pelo Spring

    // ...

    private SecretsManagerProperties map(final GetSecretValueResponse response) {
        final var value = response.getValueForField("SecretString", String.class);

        if (value.isEmpty()) {
            final var message = "Error in obtaining information from Secrets Manager";
            log.error(message);
            throw new ConfigurationException(message);
        }

        try {
            return objectMapper.readValue(value.get(), SecretsManagerProperties.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Secrets Manager response", e);
            throw new ConfigurationException("Failed to parse Secrets Manager response");
        }
    }
}
```

---

## Verificação

- [ ] Remover import `com.google.gson.*` de todos os arquivos
- [ ] Confirmar que `./mvnw package` compila sem erros
- [ ] Subir a aplicação localmente com o Localstack e verificar que as credenciais do banco são carregadas corretamente pelo Secrets Manager
