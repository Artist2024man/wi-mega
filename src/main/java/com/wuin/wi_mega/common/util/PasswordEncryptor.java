package com.wuin.wi_mega.common.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class PasswordEncryptor {
    // 配置
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    // 生成盐值
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // 加密密码
    public static String hashPassword(String password, String salt) {
        try {
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = keyFactory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Throwable t) {
            log.error("hash pwd exception", t);
            throw new RuntimeException("password encrypt error");
        }
    }

    // 验证密码
    public static boolean verifyPassword(String password, String encryptedPassword, String salt) {
        try {
            String newEncryptedPassword = hashPassword(password, salt);
            return newEncryptedPassword.equals(encryptedPassword);
        }catch (Throwable t) {
            log.error("check pwd exception", t);
            return false;
        }
    }
}


