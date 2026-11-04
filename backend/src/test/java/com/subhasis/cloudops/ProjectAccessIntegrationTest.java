package com.subhasis.cloudops;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.subhasis.cloudops.entity.Role;
import com.subhasis.cloudops.entity.User;
import com.subhasis.cloudops.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

class ProjectAccessIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository users;

    private String projectName() {
        return "project-" + UUID.randomUUID();
    }

    // ----- authentication -----

    @Test
    void protectedEndpointsRejectRequestsWithoutToken() throws Exception {
        assertThat(status(send(get("/api/projects"), null, null))).isEqualTo(401);
        assertThat(status(send(post("/api/projects"), null, projectJson("x", "ACTIVE")))).isEqualTo(401);
        assertThat(status(send(get("/api/dashboard/stats"), null, null))).isEqualTo(401);
    }

    @Test
    void garbageTokenIsRejected() throws Exception {
        assertThat(status(send(get("/api/projects"), "abc.def.ghi", null))).isEqualTo(401);
    }

    @Test
    void tamperedTokenIsRejected() throws Exception {
        String token = registerAndGetToken(uniqueEmail());
        String[] parts = token.split("\\.");
        char first = parts[2].charAt(0);
        parts[2] = (first == 'A' ? 'B' : 'A') + parts[2].substring(1);
        String tampered = String.join(".", parts);

        assertThat(status(send(get("/api/projects"), tampered, null))).isEqualTo(401);
    }

    // ----- CRUD for the owner -----

    @Test
    void ownerCanCreateAndListProject() throws Exception {
        String email = uniqueEmail();
        String token = registerAndGetToken(email);
        String name = projectName();

        MvcResult created = send(post("/api/projects"), token, projectJson(name, "ACTIVE"));
        assertThat(status(created)).isEqualTo(201);
        assertThat(body(created)).contains(name).contains("\"ownerEmail\":\"" + email + "\"");

        MvcResult list = send(get("/api/projects"), token, null);
        assertThat(status(list)).isEqualTo(200);
        assertThat(body(list)).contains(name);
    }

    @Test
    void ownerCanUpdateAndDeleteProject() throws Exception {
        String token = registerAndGetToken(uniqueEmail());
        long id = createProject(token, projectName(), "ACTIVE");

        MvcResult updated = send(put("/api/projects/" + id), token, projectJson(projectName(), "COMPLETED"));
        assertThat(status(updated)).isEqualTo(200);
        assertThat(body(updated)).contains("\"status\":\"COMPLETED\"");

        assertThat(status(send(delete("/api/projects/" + id), token, null))).isEqualTo(204);
        assertThat(status(send(get("/api/projects/" + id), token, null))).isEqualTo(404);
    }

    @Test
    void emptyNameIsRejected() throws Exception {
        String token = registerAndGetToken(uniqueEmail());

        MvcResult r = send(post("/api/projects"), token, projectJson("", "ACTIVE"));
        assertThat(status(r)).isEqualTo(400);
        assertThat(body(r)).contains("name is required");
    }

    @Test
    void missingProjectReturnsNotFound() throws Exception {
        String token = registerAndGetToken(uniqueEmail());

        MvcResult r = send(get("/api/projects/999999999"), token, null);
        assertThat(status(r)).isEqualTo(404);
        assertThat(body(r)).contains("not found");
    }

    // ----- authorization: users must not touch each other's data -----

    @Test
    void userCannotSeeOrChangeAnotherUsersProject() throws Exception {
        String tokenA = registerAndGetToken(uniqueEmail());
        String tokenB = registerAndGetToken(uniqueEmail());
        long id = createProject(tokenA, projectName(), "ACTIVE");

        // B sees an empty list
        MvcResult list = send(get("/api/projects"), tokenB, null);
        assertThat(status(list)).isEqualTo(200);
        assertThat(body(list)).isEqualTo("[]");

        // B gets 404 (not 403) on A's project for every operation
        assertThat(status(send(get("/api/projects/" + id), tokenB, null))).isEqualTo(404);
        assertThat(status(send(put("/api/projects/" + id), tokenB, projectJson("hacked", "COMPLETED")))).isEqualTo(404);
        assertThat(status(send(delete("/api/projects/" + id), tokenB, null))).isEqualTo(404);

        // A's project is untouched
        MvcResult stillThere = send(get("/api/projects/" + id), tokenA, null);
        assertThat(status(stillThere)).isEqualTo(200);
        assertThat(body(stillThere)).doesNotContain("hacked");
    }

    @Test
    void adminCanSeeEveryonesProjects() throws Exception {
        String tokenA = registerAndGetToken(uniqueEmail());
        String adminEmail = uniqueEmail();
        String tokenAdmin = registerAndGetToken(adminEmail);
        String name = projectName();
        long id = createProject(tokenA, name, "ACTIVE");

        User admin = users.findByEmail(adminEmail).orElseThrow();
        admin.setRole(Role.ADMIN);
        users.save(admin);

        MvcResult list = send(get("/api/projects"), tokenAdmin, null);
        assertThat(status(list)).isEqualTo(200);
        assertThat(body(list)).contains(name);
        assertThat(status(send(get("/api/projects/" + id), tokenAdmin, null))).isEqualTo(200);
    }
}
