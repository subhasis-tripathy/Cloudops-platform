package com.subhasis.cloudops;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.subhasis.cloudops.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository users;

    @Test
    void registerReturnsTokenAndUserRole() throws Exception {
        String email = uniqueEmail();
        MvcResult r = send(post("/api/auth/register"), null, registerJson("Test", email, "password123"));

        assertThat(status(r)).isEqualTo(201);
        assertThat(body(r)).contains("\"token\":\"").contains("\"role\":\"USER\"").contains(email);
        assertThat(body(r)).doesNotContain("password");
    }

    @Test
    void passwordIsStoredAsBcryptHash() throws Exception {
        String email = uniqueEmail();
        registerAndGetToken(email);

        String stored = users.findByEmail(email).orElseThrow().getPassword();
        assertThat(stored).isNotEqualTo("password123");
        assertThat(stored).startsWith("$2");
    }

    @Test
    void duplicateEmailIsRejectedWithConflict() throws Exception {
        String email = uniqueEmail();
        registerAndGetToken(email);

        MvcResult r = send(post("/api/auth/register"), null, registerJson("Again", email, "password123"));
        assertThat(status(r)).isEqualTo(409);
    }

    @Test
    void shortPasswordIsRejected() throws Exception {
        MvcResult r = send(post("/api/auth/register"), null, registerJson("Test", uniqueEmail(), "abc"));
        assertThat(status(r)).isEqualTo(400);
        assertThat(body(r)).contains("password");
    }

    @Test
    void invalidEmailIsRejected() throws Exception {
        MvcResult r = send(post("/api/auth/register"), null, registerJson("Test", "not-an-email", "password123"));
        assertThat(status(r)).isEqualTo(400);
    }

    @Test
    void registerCannotChooseAdminRole() throws Exception {
        String json = "{\"name\":\"Sneaky\",\"email\":\"" + uniqueEmail()
                + "\",\"password\":\"password123\",\"role\":\"ADMIN\"}";
        MvcResult r = send(post("/api/auth/register"), null, json);

        // Either the extra field is ignored (account created as USER) or the request is rejected.
        // What must never happen is an ADMIN account being created.
        assertThat(status(r)).isIn(201, 400);
        if (status(r) == 201) {
            assertThat(body(r)).contains("\"role\":\"USER\"");
        }
    }

    @Test
    void loginWithCorrectPasswordReturnsToken() throws Exception {
        String email = uniqueEmail();
        registerAndGetToken(email);

        MvcResult r = send(post("/api/auth/login"), null,
                "{\"email\":\"" + email + "\",\"password\":\"password123\"}");
        assertThat(status(r)).isEqualTo(200);
        assertThat(body(r)).contains("\"token\":\"");
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        String email = uniqueEmail();
        registerAndGetToken(email);

        MvcResult r = send(post("/api/auth/login"), null,
                "{\"email\":\"" + email + "\",\"password\":\"wrong-password\"}");
        assertThat(status(r)).isEqualTo(401);
        assertThat(body(r)).contains("Invalid email or password");
    }

    @Test
    void loginWithUnknownUserIsUnauthorized() throws Exception {
        MvcResult r = send(post("/api/auth/login"), null,
                "{\"email\":\"" + uniqueEmail() + "\",\"password\":\"password123\"}");
        assertThat(status(r)).isEqualTo(401);
    }
}
