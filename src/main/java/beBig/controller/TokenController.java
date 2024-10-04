package beBig.controller;


import beBig.service.jwt.JwtTokenProvider;
import beBig.service.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin("*")
@Controller
@RequestMapping("/token")
@Slf4j
@RequiredArgsConstructor
public class TokenController {

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping()
    public ResponseEntity<Map<String, Object>> refreshToken(
            @RequestHeader("Authorization") String token, HttpServletRequest request) {

        String refreshToken = request.getHeader("refreshToken");
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Access Token이 유효한 경우
            if (jwtTokenProvider.validateToken(token)) {
                response.put("accessToken", token);
                return ResponseEntity.ok(response);
            }

            // 2. Access Token이 유효하지 않고 Refresh Token만 유효한 경우 -> 새 Access Token 생성
            if (jwtTokenProvider.checkRefreshToken(refreshToken)) {
                String newAccessToken = jwtTokenProvider.refreshAccessToken(refreshToken);
                response.put("accessToken", newAccessToken);
                return ResponseEntity.ok(response);
            }

            // 3. 둘 다 유효하지 않은 경우
            response.put("error", "Both tokens are invalid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);  // 401 Unauthorized 상태 반환

        } catch (Exception e) {
            // 에러 발생 시, 로그 출력 및 500 Internal Server Error 반환
            log.error("error: {}", e.getMessage());
            response.put("error", "Internal Server Error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
