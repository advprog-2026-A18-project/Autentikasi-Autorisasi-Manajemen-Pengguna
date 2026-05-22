package my_sawit.authentication_manajemen_akun.auth.functional;

import io.restassured.RestAssured;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(SerenityJUnit5Extension.class)
@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:auth-functional-test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=false",
                "grpc.server.enabled=false",
                "app.profiling-seeder.enabled=false",
                "app.admin.username=admin_functional",
                "app.admin.email=admin.functional@mysawit.test",
                "app.admin.password=Password123!",
                "app.admin.fullname=Admin Functional",
                "app.google.clientId=test-google-client-id",
                "jwt.secret=0123456789012345678901234567890123456789012345678901234567890123",
                "jwt.expirationMs=600000",
                "jwt.refreshExpirationMs=604800000"
        }
)
class AuthFunctionalTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void registerLoginRefreshAndLogoutFlowShouldSucceed() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "functional.buruh." + suffix + "@mysawit.test";
        String username = "functional_buruh_" + suffix;

        SerenityRest.given()
                .contentType("application/json")
                .body("""
                        {
                          "username": "%s",
                          "fullname": "Functional Buruh",
                          "email": "%s",
                          "password": "Password123!",
                          "role": "BURUH"
                        }
                        """.formatted(username, email))
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201)
                .body("statusCode", equalTo(201))
                .body("data.accessToken", notNullValue())
                .body("data.refreshToken", notNullValue())
                .body("data.user.email", equalTo(email));

        String refreshToken = SerenityRest.given()
                .contentType("application/json")
                .body("""
                        {
                          "email": "%s",
                          "password": "Password123!"
                        }
                        """.formatted(email))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("statusCode", equalTo(200))
                .body("data.accessToken", notNullValue())
                .body("data.refreshToken", notNullValue())
                .extract()
                .path("data.refreshToken");

        SerenityRest.given()
                .contentType("application/json")
                .body("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(refreshToken))
                .when()
                .post("/auth/refresh")
                .then()
                .statusCode(200)
                .body("statusCode", equalTo(200))
                .body("data.accessToken", notNullValue());

        SerenityRest.given()
                .contentType("application/json")
                .body("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(refreshToken))
                .when()
                .post("/auth/logout")
                .then()
                .statusCode(200)
                .body("statusCode", equalTo(200));

        SerenityRest.given()
                .contentType("application/json")
                .body("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(refreshToken))
                .when()
                .post("/auth/refresh")
                .then()
                .statusCode(401)
                .body("statusCode", equalTo(401));
    }
}
