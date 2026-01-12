package com.wuin.wi_mega.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Slf4j
public class AESUtils {

    /**
     * 编码
     */
    private static final String ENCODING = "UTF-8";

    /**
     * 算法定义
     */
    private static final String AES_ALGORITHM = "AES";

    /**
     * 指定填充方式
     */
    private static final String CIPHER_CBC_PADDING = "AES/CBC/PKCS5Padding";

    /**
     * AES_CBC加密
     *
     * @param content 待加密内容
     * @param aesKey  密码
     * @return
     */
    public static String encryptCBC(String content, String aesKey, String ivSeed) {
        if (StringUtils.isBlank(content)) {
            log.warn("AES_CBC encrypt: the content is null!");
            throw new IllegalArgumentException("加密内容为空!!!");
        }

        if (StringUtils.isNotBlank(aesKey)) {
            try {
                //对密码进行编码
                byte[] bytes = aesKey.getBytes(ENCODING);
                //设置加密算法，生成秘钥
                SecretKeySpec skeySpec = new SecretKeySpec(bytes, AES_ALGORITHM);
                // "算法/模式/补码方式"
                Cipher cipher = Cipher.getInstance(CIPHER_CBC_PADDING);
                //偏移
                IvParameterSpec iv = new IvParameterSpec(ivSeed.getBytes(ENCODING));
                //选择加密
                cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
                //根据待加密内容生成字节数组
                byte[] encrypted = cipher.doFinal(content.getBytes(ENCODING));
                //返回base64字符串
                return Base64.getEncoder().encodeToString(encrypted);
            } catch (Exception e) {
                log.error("AES_CBC encrypt exception", e);
                throw new RuntimeException("数据链接加密异常!!!");
            }
        } else {
            throw new IllegalArgumentException("aesKey不可为空!!!");
        }
    }

    /**
     * AES_CBC解密
     *
     * @param content 待解密内容
     * @param aesKey  密码
     * @return
     */
    public static String decryptCBC(String content, String aesKey, String ivSeed) {
        if (StringUtils.isBlank(content)) {
            throw new IllegalArgumentException("解密内容为空!!!");
        }

        if (StringUtils.isNotBlank(aesKey)) {
            try {
                //对密码进行编码
                byte[] bytes = aesKey.getBytes(ENCODING);
                //设置解密算法，生成秘钥
                SecretKeySpec skeySpec = new SecretKeySpec(bytes, AES_ALGORITHM);
                //偏移
                IvParameterSpec iv = new IvParameterSpec(ivSeed.getBytes(ENCODING));
                // "算法/模式/补码方式"
                Cipher cipher = Cipher.getInstance(CIPHER_CBC_PADDING);
                //选择解密
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
                //先进行Base64解码
                byte[] decodeBase64 = Base64.getDecoder().decode(content);
                //根据待解密内容进行解密
                byte[] decrypted = cipher.doFinal(decodeBase64);
                //将字节数组转成字符串
                return new String(decrypted, ENCODING);
            } catch (Exception e) {
                log.error("AES_CBC decrypt exception:", e);
                throw new IllegalArgumentException("解密异常!!!");
            }
        } else {
            throw new IllegalArgumentException("aesKey不可为空!!!");
        }
    }
}