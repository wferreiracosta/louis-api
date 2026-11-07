package br.com.wferreiracosta.louis.controllers;

import br.com.wferreiracosta.louis.models.parameters.UserParameter;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
class PasswordStorageIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired org.springframework.context.ApplicationContext context;

    private String register(String route, String password) throws Exception {
        String id = UUID.randomUUID().toString();
        String body = mvc.perform(post("/users/" + route).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new UserParameter("A", "B", id, id + "@mail.com", password))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordEncoding").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        var user = users.findByDocument(id).orElseThrow();
        assertThat(user.getPassword()).startsWith("$2a$10$").isNotEqualTo(password);
        assertThat(encoder.matches(password, user.getPassword())).isTrue();
        assertThat(encoder.matches("wrong", user.getPassword())).isFalse();
        assertThat(user.getPasswordEncoding()).isEqualTo("BCRYPT");
        assertThat(body).doesNotContain(user.getPassword(), "password_encoding");
        return user.getPassword();
    }

    @ParameterizedTest
    @ValueSource(strings = {"common", "merchants"})
    void storesHashAndUsesIndividualSalt(String route, CapturedOutput output) throws Exception {
        String sentinel = "PRIVATE_PASSWORD_SENTINEL";
        String first = register(route, sentinel);
        assertThat(register(route, sentinel)).isNotEqualTo(first);
        register(route, "é".repeat(36));
        register(route, "x".repeat(72));
        register(route, first);
        mvc.perform(get("/users/" + route + "/page")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(first))));
        assertThat(output.getAll()).doesNotContain(sentinel, first);
    }

    @ParameterizedTest
    @ValueSource(strings = {"common", "merchants"})
    void rejectsOversizedPasswordWithoutPersistence(String route) throws Exception {
        for (String password : new String[]{"x".repeat(73), "é".repeat(37)}) {
            long count = users.count();
            mvc.perform(post("/users/" + route).contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(new UserParameter("A", "B", "new", "new@a", password))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].fieldName").value("password"))
                    .andExpect(jsonPath("$.errors[0].message").value("Password must be at most 72 UTF-8 bytes"));
            assertThat(users.count()).isEqualTo(count);
        }
    }

    @Test
    void parsingErrorsDoNotDiscloseValues(CapturedOutput output) throws Exception {
        assertThat(new UserParameter("A", "B", "1", "a@b", "PRIVATE_JSON_SENTINEL").toString())
                .doesNotContain("PRIVATE_JSON_SENTINEL");
        for (String json : new String[]{"{\"password\":\"PRIVATE_JSON_SENTINEL\",",
                "{\"password\":{\"PRIVATE_JSON_SENTINEL\":true}}"}) {
            mvc.perform(post("/users/common").contentType(MediaType.APPLICATION_JSON).content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Malformed JSON request"));
        }
        assertThat(output.getAll()).doesNotContain("PRIVATE_JSON_SENTINEL");
    }

    @Test
    void openApiStillWorksAfterBootUpgrade() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.openapi").exists());
    }

    @Test
    void normalApplicationDoesNotRegisterOfflineMigration() {
        assertThat(context.getBeansOfType(br.com.wferreiracosta.migration.PasswordMigration.class)).isEmpty();
        assertThat(Arrays.stream(context.getBeanDefinitionNames())
                .noneMatch(name -> name.contains("MigrationConfiguration"))).isTrue();
    }
}
