package beBig.service.jwt;

import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtUtil(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Long extractUserIdFromToken(String token) {
        // 토큰에서 "Bearer " 부분을 제거한 후 userId 추출
        return jwtTokenProvider.getUserIdFromJWT(token.replace("Bearer ", ""));
    }
}
