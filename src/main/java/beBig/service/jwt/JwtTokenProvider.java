package beBig.service.jwt;

import beBig.mapper.UserMapper;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    // io.jsonwebtoken.security.Keys를 통해 안전한 시크릿 키 생성
    private final SecretKey jwtSecret = Keys.secretKeyFor(SignatureAlgorithm.HS256); // 256비트 시크릿 키 생성
    private final long jwtExpirationInMs = 1800000; // 토큰 유효시간 30분
    private final long refreshTokenExpirationInMs = 604800000; // 7일 (1주일)
    private final UserMapper userMapper;
//    private final JwtUtil jwtUtil;

    // JWT 토큰 생성
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        generateRefreshToken(userId);

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

    // --- 신규추가 --- //

    // 로그인요청 -> refreshToken 생성해서 db에 저장
    // 요청올때마다
    //  accessToken 검사 -> 유효 -> 그대로진행
    //                  -> 유효하지 않을시 checkRefreshToken 실행
    //                  -> checkRefreshToken 이 true일시 generateRefreshToken 새로운 accessToken을 가지고 진행
    //  만약 유효하지 않을시 -오류값 return

    // Refresh Token 생성
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationInMs);
        String refreshToken = Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecret)
                .compact();
        //refreshToken 테이블에 저장후 user테이블에 저장
        userMapper.saveRefreshTokenRT(userId, refreshToken, expiryDate);
        userMapper.saveRefreshTokenUser(userId, refreshToken);
        return refreshToken;
    }

    // Refresh Token 검증 및 새로운 Access Token 발급
    public String refreshAccessToken(String refreshToken) {
        if (checkRefreshToken(refreshToken)) {
            Long userId = userMapper.getUserIdByRefreshToken(refreshToken);
            return generateToken(userId);
        }
        throw new JwtException("Invalid Refresh Token");
    }

    // db에서 refreshToken 지우기 (로그아웃시)
    public void removeRefreshToken(String refreshToken) {
        userMapper.clearRefreshTokenRT(refreshToken);
        userMapper.clearRefreshTokenUser(refreshToken);
    }

    //accessToken 검사
    public long checkAccessToken(String token) {
//        if (validateToken(token)) return jwtUtil.extractUserIdFromToken(token);
        return -1;
    }

    // refreshToken 검사
    public boolean checkRefreshToken(String refreshToken) {
        LocalDateTime expiryDate = userMapper.getExpiredTimeByRefreshToken(refreshToken);
        LocalDateTime now = LocalDateTime.now();
        return !expiryDate.isBefore(now);
    }

    // 통합된 토큰 검증 및 리프레시 처리 메서드
    public String validateAndRefreshToken(String accessToken, String refreshToken) {
        // 액세스 토큰 검증
        if (validateToken(accessToken)) {
            // 토큰이 유효하면 그대로 반환
            return accessToken;
        } else {
            // 액세스 토큰이 만료되었을 때 리프레시 토큰을 검증
            if (checkRefreshToken(refreshToken)) {
                // 리프레시 토큰이 유효하다면 새로운 액세스 토큰 발급
                String newAccessToken = refreshAccessToken(refreshToken);
                return newAccessToken;
            } else {
                // 리프레시 토큰이 유효하지 않다면 예외 발생
                throw new JwtException("Token has expired, please log in again.");
            }
        }
    }

}
