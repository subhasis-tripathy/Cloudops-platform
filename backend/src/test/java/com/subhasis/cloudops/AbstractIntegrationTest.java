package com.subhasis.cloudops;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Base class for integration tests.
 * - Starts the full Spring context against the real PostgreSQL database.
 * - @Transactional rolls back every test, so no test data is left behind.
 * - Uses a test-only JWT secret, so tests do not depend on your JWT_SECRET variable.
 */
@SpringBootTest
@Transactional
@TestPropertySource(properties = "app.jwt.secret=Y2ktb25seS1kdW1teS1zZWNyZXQtbm90LWZvci1wcm9kLTAxMjM0NTY3ODk=")
public abstract class AbstractIntegrationTest {

    static {
        // The Postgres driver rejects the old zone name "Asia/Calcutta", so run tests in UTC
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");
    private static final Pattern ID = Pattern.compile("\"id\":(\\d+)");

    @Autowired
    private WebApplicationContext context;

    protected MockMvc mvc;

    @BeforeEach
    void setUpMockMvc() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    // ----- helpers -----

    protected String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@test.local";
    }

    protected String registerJson(String name, String email, String password) {
        return "{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    protected String projectJson(String name, String status) {
        return "{\"name\":\"" + name + "\",\"description\":\"test\",\"status\":\"" + status + "\"}";
    }

    /** Sends a request, optionally with a Bearer token and a JSON body. */
    protected MvcResult send(MockHttpServletRequestBuilder request, String token, String body) throws Exception {
        if (token != null) request.header("Authorization", "Bearer " + token);
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(body);
        return mvc.perform(request).andReturn();
    }

    protected int status(MvcResult result) {
        return result.getResponse().getStatus();
    }

    protected String body(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString();
    }

    protected String extractToken(String responseBody) {
        Matcher m = TOKEN.matcher(responseBody);
        assertThat(m.find()).as("response should contain a token: " + responseBody).isTrue();
        return m.group(1);
    }

    protected String registerAndGetToken(String email) throws Exception {
        MvcResult r = send(post("/api/auth/register"), null, registerJson("Test User", email, "password123"));
        assertThat(status(r)).isEqualTo(201);
        return extractToken(body(r));
    }

    protected long createProject(String token, String name, String status) throws Exception {
        MvcResult r = send(post("/api/projects"), token, projectJson(name, status));
        assertThat(status(r)).isEqualTo(201);
        Matcher m = ID.matcher(body(r));
        assertThat(m.find()).isTrue();
        return Long.parseLong(m.group(1));
    }
}
