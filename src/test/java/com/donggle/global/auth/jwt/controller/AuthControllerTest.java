package com.donggle.global.auth.jwt.controller;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.donggle.domain.user.dto.TokenResponse;
import com.donggle.domain.user.dto.UserLoginRequest;
import com.donggle.domain.user.dto.UserSignupRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("인증 API 테스트")
class AuthControllerTest {

    @LocalServerPort int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 회원가입_성공_DTO_사용() {
        UserSignupRequest signupRequest =
                new UserSignupRequest("testuser3@example.com", "Test1234!", "202412347", "테스트유저");

        given().contentType(ContentType.JSON)
                .body(signupRequest)
                .when()
                .post("/api/auth/signup")
                .then()
                .statusCode(201);
    }

    @Test
    void 로그인_성공_DTO_사용() {
        // 사전 회원가입
        UserSignupRequest signupRequest =
                new UserSignupRequest("testuser4@example.com", "Test1234!", "202412348", "테스트유저");
        given().contentType(ContentType.JSON).body(signupRequest).post("/api/auth/signup");

        // 로그인
        UserLoginRequest loginRequest = new UserLoginRequest("testuser4@example.com", "Test1234!");

        TokenResponse tokenResponse =
                given().contentType(ContentType.JSON)
                        .body(loginRequest)
                        .when()
                        .post("/api/auth/login")
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(TokenResponse.class);

        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.getAccessToken()).isNotNull();
        assertThat(tokenResponse.getRefreshToken()).isNotNull();
    }
}
