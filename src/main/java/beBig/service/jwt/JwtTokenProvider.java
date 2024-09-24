package beBig.service.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // io.jsonwebtoken.security.Keys를 통해 안전한 시크릿 키 생성
    private final SecretKey jwtSecret = Keys.secretKeyFor(SignatureAlgorithm.HS256); // 256비트 시크릿 키 생성
    private final long jwtExpirationInMs = 1800000; // 토큰 유효시간 30분

    // JWT 토큰 생성
    public String generateToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userId)  // 토큰에 userId 저장
                .setIssuedAt(new Date())  // 토큰 발행 시간
                .setExpiration(expiryDate)  // 토큰 만료 시간
                .signWith(jwtSecret)  // 서명 알고리즘과 시크릿 키 설정
                .compact();
    }

    // 토큰에서 사용자 아이디 추출
    public String getUserIdFromJWT(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecret)  // 시크릿 키로 서명 검증
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 유저명 추출
    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
