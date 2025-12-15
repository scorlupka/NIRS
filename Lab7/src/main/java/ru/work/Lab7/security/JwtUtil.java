package ru.work.Lab7.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {
    private String secret = "suuuuuuuperlooooooongseeeeeecreeeeeeetkeeeeeeeeyyyyyfooooooormyyyyyyyyyyyyyytooooooooookkeeeeeeeeen";
    private SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    private int lifetime = 864000000;

    public String generateToken(int id, String role) {
        return Jwts.builder()
                .setSubject(Integer.toString(id))
                .claim("role", role)
                .setExpiration(new Date(System.currentTimeMillis() + lifetime)) // 1 день
                .signWith(key)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractId(String token) {
        return extractClaim(token, Claims::getSubject);
    }
}
