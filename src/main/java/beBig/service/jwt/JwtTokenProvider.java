package beBig.service.jwt;

import beBig.mapper.RefreshTokenMapper;
import beBig.vo.RefreshTokenVo;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // io.jsonwebtoken.security.Keys를 통해 안전한 시크릿 키 생성
    private final SecretKey jwtSecret = Keys.secretKeyFor(SignatureAlgorithm.HS256); // 256비트 시크릿 키 생성
    private final long jwtExpirationInMs = 1800000; // 토큰 유효시간 30분

    @Autowired
    private RefreshTokenMapper refreshTokenMapper;

    // JWT 토큰 생성
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .claim("userId", userId)   // userId를 클레임에 저장
                .setIssuedAt(new Date())   // 토큰 발행 시간
                .setExpiration(expiryDate) // 토큰 만료 시간
                .signWith(jwtSecret)       // 서명 알고리즘과 시크릿 키 설정
                .compact();
    }

    // 토큰에서 userId 추출
    public Long getUserIdFromJWT(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecret)  // 시크릿 키로 서명 검증
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", Long.class);  // 클레임에서 userId 추출
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // jwt -> userId 보관
    // jwt 만료시 -> refreshToken
    // refreshToken 대체 -> 통해서 새로운걸 생성
    // token -> token -> userId
    // token userId 암호화
    // jwt 만료 -> refreshToken -> 이때 userId는 어디서 가져와야하나
    // refreshToken -> db 저장 만료가 -> db refreshToken 있는지 확인하고 보내라
    // refreshToken -> db에 user_id col -> refreshToken 사용할때 가져와서 사용하는 방식
    // 다른 방식

    // refreshToken 보관
    // local storage 에 보관
    // 만료 -> local refreshToken -> local refreshToken null -> logout
    // ! null -> refreshToken db -> refreshToken과 일치하는 col -> userId
    // refreshToken + userId로 토큰발급
    // 새로운 jwt 발급 -> refreshToken 새로해야되나요?

    // refreshToken - 사용자정보 x sk
    // db - access refreshToken
    // db - refreshToken 선택의 문제
    // db -


    // Access Token 재발급
    public String refreshAccessToken(String refreshToken) {
        if (validateToken(refreshToken)) {
            Long userId = getUserIdFromJWT(refreshToken);

            // Refresh Token이 DB에 존재하는지 확인
            RefreshTokenVo storedToken = refreshTokenMapper.findByToken(refreshToken);
            if (storedToken == null) {
                throw new JwtException("Invalid Refresh Token");
            }

            // Refresh Token 만료 여부 확인
            if (storedToken.getExpiryDate().before(new Date())) {
                throw new JwtException("Refresh Token has expired");
            }

            // 새로운 Access Token 생성
            return generateToken(userId);
        }
        throw new JwtException("Invalid Refresh Token");
    }

    // Refresh Token 삭제 (로그아웃 시 사용)
    public void deleteRefreshToken(String refreshToken) {
        refreshTokenMapper.deleteByToken(refreshToken);
    }
}
