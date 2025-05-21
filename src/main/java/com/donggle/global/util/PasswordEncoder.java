package com.donggle.global.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder {

    private static final int SALT_SIZE = 16;
    private static final String ALGORITHM = "SHA-256";

    /**
     * 비밀번호를 암호화합니다.
     *
     * @param plainPassword 암호화할 비밀번호
     * @return salt와 해시된 비밀번호가 결합된 문자열 (Base64 인코딩)
     */
    public String encode(String plainPassword) {
        try {
            byte[] salt = generateSalt();
            byte[] hashedPassword = hashPassword(plainPassword, salt);

            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("비밀번호 암호화 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 저장된 암호화된 비밀번호와 사용자 입력 비밀번호가 일치하는지 확인합니다.
     *
     * @param plainPassword 사용자 입력 비밀번호
     * @param encodedPassword 저장된 암호화된 비밀번호
     * @return 일치 여부
     */
    public boolean matches(String plainPassword, String encodedPassword) {
        try {
            byte[] combined = Base64.getDecoder().decode(encodedPassword);

            byte[] salt = new byte[SALT_SIZE];
            System.arraycopy(combined, 0, salt, 0, salt.length);

            byte[] hashedPassword = hashPassword(plainPassword, salt);

            // 저장된 해시와 새로 계산한 해시 비교
            for (int i = 0; i < hashedPassword.length; i++) {
                if (hashedPassword[i] != combined[salt.length + i]) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_SIZE];
        random.nextBytes(salt);
        return salt;
    }

    private byte[] hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        md.update(salt);
        return md.digest(password.getBytes());
    }
}
