package com.donggle.global.auth.jwt.repository;

import com.donggle.global.auth.jwt.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface TokenRepository extends CrudRepository<RefreshToken, Long> {

    boolean existsByToken(String refreshToken);
}
