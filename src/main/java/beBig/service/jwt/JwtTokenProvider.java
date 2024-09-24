package beBig.service.jwt;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;


@Component
public class JwtTokenProvider {
    private final String jwtSecret = "SECRETKEYSECRETKEYSECRETKEYSECRETKEYSECRETKEY";
    private final long jwtExpirationInMs = 18000000; //30분

    // JWT 토큰 생성
    public String generateToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userId)  // 토큰에 userId를 저장
                .setIssuedAt(new Date())  // 토큰 발행 시간
                .setExpiration(expiryDate)  // 토큰 만료 시간
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)  // 서명 알고리즘과 시크릿 키 설정
                .compact();
    }

    // 토큰에서 사용자 아이디 추출
    public String getUserIdFromJWT(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
