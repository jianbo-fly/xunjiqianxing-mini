package com.xunjiqianxing.app.service;

import com.xunjiqianxing.common.constant.Constants;
import com.xunjiqianxing.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final StringRedisTemplate redisTemplate;

    private static final long CODE_EXPIRE_MINUTES = 5;
    private static final long SEND_INTERVAL_SECONDS = 60;

    public void sendVerifyCode(String phone) {
        String intervalKey = Constants.RedisKey.RATE_LIMIT + "sms:" + phone;
        Boolean exists = redisTemplate.hasKey(intervalKey);
        if (exists) {
            throw new BizException("验证码发送过于频繁，请稍后再试");
        }

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));

        String codeKey = Constants.RedisKey.SMS_CODE + phone;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(intervalKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // TODO: 接入真实短信服务商（阿里云/腾讯云）发送短信
        log.info("发送验证码: phone={}, code={}", phone, code);
    }

    public void verifyCode(String phone, String code) {
        String codeKey = Constants.RedisKey.SMS_CODE + phone;
        String storedCode = redisTemplate.opsForValue().get(codeKey);

        if (storedCode == null) {
            throw new BizException("验证码已过期，请重新获取");
        }
        if (!storedCode.equals(code)) {
            throw new BizException("验证码错误");
        }

        redisTemplate.delete(codeKey);
    }
}
