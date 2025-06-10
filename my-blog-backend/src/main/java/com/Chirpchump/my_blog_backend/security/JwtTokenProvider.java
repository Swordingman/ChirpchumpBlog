package com.Chirpchump.my_blog_backend.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwtSecret}") // 从 application.properties 读取
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}") // 从 application.properties 读取
    private int jwtExpirationInMs;

    private SecretKey getSigningKey() {
        // 使用更安全的方式生成或存储密钥
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    // 可选：重载一个直接接收UserDetails的方法，用于手动创建用户后生成token
    public String generateTokenFromUserDetails(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }


    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("无效的JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("过期的JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("不支持的JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims 字符串为空");
        } // io.jsonwebtoken.security.SecurityException for signature issues
        catch (io.jsonwebtoken.security.SignatureException ex) {
            logger.error("JWT 签名验证失败");
        }
        return false;
    }
}