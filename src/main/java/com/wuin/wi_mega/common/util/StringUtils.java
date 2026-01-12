package com.wuin.wi_mega.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class StringUtils {

    public static String encodeToMD5(String input) {
        try {
            // 创建MD5消息摘要实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 将输入字符串转换为字节数组
            byte[] messageDigest = md.digest(input.getBytes());
            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}