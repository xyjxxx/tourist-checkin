package com.travel.service;

import com.travel.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    private static final String CODE_PREFIX = "email:code:";
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final String FROM_ADDRESS = "1811870087@qq.com";
    private static final String FROM_NAME = "拾光旅记";

    /**
     * 发送验证码邮件
     */
    public void sendVerificationCode(String email) {
        // 检查发送频率限制（60秒内不能重复发送）
        String rateKey = CODE_PREFIX + "rate:" + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateKey))) {
            throw new BadRequestException("验证码发送过于频繁，请60秒后再试");
        }

        // 生成6位验证码
        String code = generateCode();

        // 存入Redis，5分钟过期
        redisTemplate.opsForValue().set(CODE_PREFIX + email, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        // 设置发送频率限制，60秒过期
        redisTemplate.opsForValue().set(rateKey, "1", 60, TimeUnit.SECONDS);

        // 发送邮件
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(FROM_ADDRESS, FROM_NAME);
            helper.setTo(email);
            helper.setSubject("【拾光旅记】密码重置验证码");
            helper.setText(buildEmailContent(code), true);
            mailSender.send(message);
            log.info("验证码已发送至: {}", email);
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage(), e);
            // 发送失败时清除验证码
            redisTemplate.delete(CODE_PREFIX + email);
            throw new BadRequestException("邮件发送失败，请稍后再试");
        }
    }

    /**
     * 验证验证码
     */
    public boolean verifyCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue().get(CODE_PREFIX + email);
        if (storedCode == null) {
            throw new BadRequestException("验证码已过期，请重新获取");
        }
        if (!storedCode.equals(code)) {
            return false;
        }
        // 验证成功后删除验证码（一次性使用）
        redisTemplate.delete(CODE_PREFIX + email);
        return true;
    }

    private String generateCode() {
        return String.format("%06d", new java.security.SecureRandom().nextInt(1000000));
    }

    private String buildEmailContent(String code) {
        return "<div style='font-family: PingFang SC, Microsoft YaHei, sans-serif; max-width: 500px; margin: 0 auto; padding: 24px;'>" +
                "<div style='text-align: center; margin-bottom: 24px;'>" +
                "<h2 style='color: #FB7299; margin: 0;'>拾光旅记</h2>" +
                "</div>" +
                "<div style='background: #F5F5F7; border-radius: 12px; padding: 24px; text-align: center;'>" +
                "<p style='color: #1D1D1F; font-size: 16px; margin-bottom: 16px;'>您正在重置密码，验证码如下：</p>" +
                "<div style='font-size: 36px; font-weight: bold; color: #FB7299; letter-spacing: 8px; padding: 16px; " +
                "background: white; border-radius: 8px; display: inline-block;'>" + code + "</div>" +
                "<p style='color: #86868B; font-size: 14px; margin-top: 16px;'>验证码 " + CODE_EXPIRE_MINUTES + " 分钟内有效，请勿泄露给他人</p>" +
                "</div>" +
                "<p style='color: #AEAEB2; font-size: 12px; text-align: center; margin-top: 16px;'>如非本人操作，请忽略此邮件</p>" +
                "</div>";
    }
}
