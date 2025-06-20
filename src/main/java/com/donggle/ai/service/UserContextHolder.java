package com.donggle.ai.service;

import lombok.experimental.UtilityClass;

/** AI Tool 함수들에서 사용자 ID에 접근하기 위한 ThreadLocal 기반 컨텍스트 홀더 */
@UtilityClass
public class UserContextHolder {

    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        userIdHolder.set(userId);
    }

    public static Long getUserId() {
        return userIdHolder.get();
    }

    public static void clear() {
        userIdHolder.remove();
    }
}
