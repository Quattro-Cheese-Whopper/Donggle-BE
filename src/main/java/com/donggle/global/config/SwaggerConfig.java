package com.donggle.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info =
                new Info()
                        .title("전남대학교 동아리 통합 모집 플랫폼 API")
                        .version("v1.0")
                        .description("전남대학교 동아리 통합 모집 플랫폼의 API 문서입니다.")
                        .contact(new Contact().name("콰트로치즈와퍼팀").email("contact@example.com"))
                        .license(
                                new License()
                                        .name("MIT License")
                                        .url("https://opensource.org/licenses/MIT"));

        // 토큰 인증 설정
        SecurityScheme securityScheme =
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        // API 그룹 태그 설정
        List<Tag> tags =
                Arrays.asList(
                        new Tag().name("인증").description("인증 관련 API"),
                        new Tag().name("회원").description("회원 관련 API"),
                        new Tag().name("동아리").description("동아리 관련 API"),
                        new Tag().name("모집").description("모집 공고 관련 API"),
                        new Tag().name("지원").description("지원서 관련 API"),
                        new Tag().name("공지").description("공지사항 관련 API"),
                        new Tag().name("파일").description("파일 관련 API"),
                        new Tag().name("알림").description("알림 관련 API"));

        // 개발/운영 서버 설정
        List<Server> servers =
                Arrays.asList(
                        new Server().url("http://localhost:8080").description("개발 서버"),
                        new Server().url("https://api.donggle.example.com").description("운영 서버"));

        return new OpenAPI()
                .info(info)
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityRequirement)
                .tags(tags)
                .servers(servers);
    }
}
