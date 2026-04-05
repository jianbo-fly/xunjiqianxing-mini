package com.xunjiqianxing.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 敏感信息加密工具（AES/ECB，用于身份证号等字段的数据库存储加密）
 *
 * 生产环境请将 app.idcard-encrypt-key 配置为随机的 16/24/32 位密钥，
 * 并通过环境变量或密钥管理服务注入，切勿使用默认值。
 */
@Slf4j
@Component
public class IdCardEncryptor {

    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    // 从配置读取，生产环境必须替换
    @Value("${app.idcard-encrypt-key:xjqx_dev_key_16b}")
    private String rawKey;

    /**
     * 加密身份证号（存储到数据库前调用）
     */
    public String encrypt(String plainText) {
        if (!StringUtils.hasText(plainText)) {
            return plainText;
        }
        try {
            SecretKeySpec keySpec = buildKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("身份证加密失败", e);
            throw new RuntimeException("数据加密失败");
        }
    }

    /**
     * 解密身份证号（从数据库读取后调用）
     */
    public String decrypt(String cipherText) {
        if (!StringUtils.hasText(cipherText)) {
            return cipherText;
        }
        // 兼容旧数据：非 Base64 格式视为未加密明文直接返回
        if (!isBase64(cipherText)) {
            return cipherText;
        }
        try {
            SecretKeySpec keySpec = buildKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 解密失败说明是旧的明文数据，直接返回原值
            log.warn("身份证解密失败，可能是旧明文数据: {}", e.getMessage());
            return cipherText;
        }
    }

    private SecretKeySpec buildKey() {
        byte[] keyBytes = new byte[16];
        byte[] raw = rawKey.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(raw, 0, keyBytes, 0, Math.min(raw.length, keyBytes.length));
        return new SecretKeySpec(keyBytes, "AES");
    }

    private boolean isBase64(String str) {
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
