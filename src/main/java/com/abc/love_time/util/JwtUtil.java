package com.abc.love_time.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
public class JwtUtil {
    // 密钥（生产环境应该配置在配置文件中）
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    // token有效期：7天
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000;

    /**
     * 生成JWT token
     * @param openid 用户openid
     * @param sessionKey 会话密钥
     * @return JWT token
     */
    public static String generateToken(String openid, String sessionKey) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("openid", openid);
        claims.put("session_key", sessionKey);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(openid)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 验证token
     * @param token JWT token
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从token中获取openid
     * @param token JWT token
     * @return openid
     */
    public static String getOpenidFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("openid", String.class);
    }
}
