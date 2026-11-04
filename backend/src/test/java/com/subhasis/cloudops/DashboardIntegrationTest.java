package com.subhasis.cloudops;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

class DashboardIntegrationTest extends AbstractIntegrationTest {

    @Test
    void newUserSeesAllZeros() throws Exception {
        String token = registerAndGetToken(uniqueEmail());

        MvcResult r = send(get("/api/dashboard/stats"), token, null);
        assertThat(status(r)).isEqualTo(200);
        assertThat(body(r))
                .contains("\"total\":0")
                .contains("\"ACTIVE\":0")
                .contains("\"COMPLETED\":0")
                .contains("\"ON_HOLD\":0")
                .contains("\"recent\":[]");
    }

    @Test
    void countsMatchTheUsersProjects() throws Exception {
        String token = registerAndGetToken(uniqueEmail());
        createProject(token, "a-" + UUID.randomUUID(), "ACTIVE");
        createProject(token, "b-" + UUID.randomUUID(), "ACTIVE");
        createProject(token, "c-" + UUID.randomUUID(), "COMPLETED");

        MvcResult r = send(get("/api/dashboard/stats"), token, null);
        assertThat(status(r)).isEqualTo(200);
        assertThat(body(r))
                .contains("\"total\":3")
                .contains("\"ACTIVE\":2")
                .contains("\"COMPLETED\":1")
                .contains("\"ON_HOLD\":0");
    }

    @Test
    void statsOnlyCountTheCurrentUsersProjects() throws Exception {
        String tokenA = registerAndGetToken(uniqueEmail());
        String tokenB = registerAndGetToken(uniqueEmail());
        createProject(tokenA, "a-" + UUID.randomUUID(), "ACTIVE");
        createProject(tokenA, "b-" + UUID.randomUUID(), "ON_HOLD");

        MvcResult r = send(get("/api/dashboard/stats"), tokenB, null);
        assertThat(status(r)).isEqualTo(200);
        assertThat(body(r)).contains("\"total\":0").contains("\"recent\":[]");
    }
}
