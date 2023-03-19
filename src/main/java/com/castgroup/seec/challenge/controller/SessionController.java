package com.castgroup.seec.challenge.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.castgroup.seec.challenge.dto.SessionInfo;

@RestController
public class SessionController {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/session")
    public ResponseEntity<SessionInfo> getSessionInfo() {
        String sessionId = getSessionId();
        long sessionTimeout = redisTemplate.getExpire(sessionId, TimeUnit.SECONDS);
        SessionInfo sessionInfo = new SessionInfo(sessionId, sessionTimeout);
        return ResponseEntity.ok(sessionInfo);
    }

    private String getSessionId() {
        String sessionId = null;
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();

        Object value = ops.get("spring:session:sessions:expires:");
        if (value instanceof byte[]) {
            sessionId = new String((byte[]) value);
        } else if (value instanceof String) {
            sessionId = (String) value;
        }

        return sessionId;
    }
}
