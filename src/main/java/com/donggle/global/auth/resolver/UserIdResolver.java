package com.donggle.global.auth.resolver;

import com.donggle.global.auth.jwt.service.JwtTokenProvider;
import com.donggle.global.error.exception.InvalidAccessTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class UserIdResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserId.class);
    }

    @Override
    public Object resolveArgument(
            @NonNull MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        String token =
                jwtTokenProvider.extractToken(webRequest.getHeader(HttpHeaders.AUTHORIZATION));
        UserId userId = parameter.getParameterAnnotation(UserId.class);

        if (userId != null && userId.required() && token == null) {
            throw InvalidAccessTokenException.EXCEPTION;
        }

        if (token == null) {
            return null;
        }

        return jwtTokenProvider.getUserIdFromAccessToken(token);
    }
}
