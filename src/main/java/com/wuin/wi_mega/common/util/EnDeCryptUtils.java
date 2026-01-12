package com.wuin.wi_mega.common.util;

import org.apache.commons.lang3.StringUtils;

public class EnDeCryptUtils {
    private static final String AES_IV = "WM2025-SWORD1210";

    private static final String AES_KEY = "WM2025-ASRWTOIRSDT-MT-QD-MT-MEGA";

    public static String encrypt(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        return AESUtils.encryptCBC(content, AES_KEY, AES_IV);
    }

    public static String decrypt(String content) {
        // 如果内容为空，直接返回
        if (StringUtils.isBlank(content)) {
            return content;
        }

        return AESUtils.decryptCBC(content, AES_KEY, AES_IV);
    }

//    public static void main(String[] args) {
//        System.out.println(decrypt("kzFP6N4uD53Hpp/HCz6/huBnCJWf2/1SK5kyCRFMmM8="));
//        System.out.println(decrypt("j4htx0CK9mWw8njCBBhE+CwOyx1RSpsBUFU9k4XLR8F/x5H8bN6Jzrp5ctMKxWry"));
//    }
}
