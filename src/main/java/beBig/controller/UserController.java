package beBig.controller;

import beBig.dto.LoginDto;
import beBig.dto.UserDto;
import beBig.dto.response.FinInfoResponseDto;

import beBig.mapper.MissionMapper;
import beBig.mapper.UserMapper;
import beBig.service.CustomUserDetails;
import beBig.service.UserService;
import beBig.service.jwt.JwtTokenProvider;
import beBig.service.jwt.JwtUtil;
import beBig.service.oauth.KakaoOauthService;
import beBig.vo.*;
import com.amazonaws.Response;
import com.google.gson.JsonObject;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spring.web.scanners.ApiListingReferenceScanner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final KakaoOauthService kakaoLoginService;
    private final UserMapper userMapper;
    private final ApiListingReferenceScanner apiListingReferenceScanner;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserDto userDto) {
        log.info("회원가입 요청: {}", userDto);
        try {
            // 필수 필드 확인 예시 (예: 이메일이 없으면 에러 반환)
            if (userDto.getEmail() == null || userDto.getEmail().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이메일은 필수 항목입니다.");
            }
            // 사용자 등록 로직 호출
            userService.registerUser(userDto);
            // 성공 응답
            return ResponseEntity.status(HttpStatus.OK).body("유저 등록 완료!");
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("회원가입 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류로 회원가입에 실패했습니다.");
        }
    }

    @GetMapping("/login/{loginUserId}")
    public ResponseEntity<String> idDuplicateCheck(@PathVariable String loginUserId) {
        boolean isDuplicated = userService.isUserLoginIdDuplicated(loginUserId);
        if (isDuplicated) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("중복된 아이디입니다.");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body("사용 가능한 아이디입니다.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginDto loginDto) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getUserLoginId(),
                            loginDto.getPassword()
                    )
            );

            log.info("Received login request for userLoginId: {}", loginDto.getUserLoginId());

            // 인증된 사용자 정보 로드
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.info("로그인 성공: {}", loginDto.getUserLoginId());


            // 로그인하기전 기존 db에 저장되어있던 refershToken이 있다면 삭제
            userService.deleteRefreshTokenBeforeLogin(userService.findUserIdByUserLoginId(loginDto.getUserLoginId()));

            // JWT 토큰 생성
            String accessToken = jwtTokenProvider.generateToken(userDetails.getUserId());

            // Refresh Token 생성 및 저장
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails.getUserId());

            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);

            log.info("JWT Accesstoken 생성: {}", accessToken);
            log.info("JWT RefreshToken 생성: {}", refreshToken);

            // 토큰을 클라이언트로 응답
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (BadCredentialsException e) {
            log.error("로그인 실패: {}", loginDto.getUserLoginId());

            response.put("error", "로그인 실패!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

        } catch (AuthenticationException e) {
            log.error("로그인 실패 - 인증 실패: {}", loginDto.getUserLoginId());

            response.put("error", "로그인 실패! 인증 실패");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }


    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // SecurityContextHolder에서 현재 인증된 사용자 정보 제거
        SecurityContextHolder.clearContext();

        // 세션 무효화
        request.getSession().invalidate();

        // JSESSIONID 쿠키 삭제
        for (javax.servlet.http.Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("JSESSIONID")) {
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }

        String refreshToken = request.getHeader("refreshToken");
        userService.removeRefreshToken(refreshToken);

        return ResponseEntity.status(HttpStatus.OK).body("로그아웃에 성공하였습니다!");
    }

    @PostMapping("/find-id")
    public ResponseEntity<String> findUserId(@RequestBody UserDto userDto) {
        String name = userDto.getName();
        String email = userDto.getEmail();

        log.info("Received name: {}, email: {}", name, email);
        String maskedUserId = userService.findUserLoginIdByNameAndEmail(name, email);

        if (maskedUserId != null) {
            log.info("Found user id: {}", maskedUserId);
            return ResponseEntity.status(HttpStatus.OK).body(maskedUserId);
        } else {
            log.info("No user found with name: {}", name);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 이름과 이메일로 등록된 아이디가 없습니다.");
        }
    }

    // 아이디, 이름, 이메일을 통해 비밀번호 찾기 요청
    @PostMapping("/find-pwd")
    public ResponseEntity<String> findPassword(@RequestBody UserDto userDto) {
        boolean isUpdated = userService.updatePasswordByEmail(userDto.getUserLoginId(), userDto.getName(), userDto.getEmail());
        if (isUpdated) {
            return ResponseEntity.status(HttpStatus.OK).body("Temporary password sent to your email!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    @GetMapping("/social-kakao")
    public ResponseEntity<?> kakaoLogin(@RequestParam("code") String code) {
        try {
            log.info("code : " + code);

            // 인가코드를 통해 AccessToken 획득
            String accessToken = kakaoLoginService.getAccessToken(code);
            // AccessToken으로 유저 정보 획득
            JsonObject userInfo = kakaoLoginService.getUserInfo(accessToken);

            if (userInfo == null) {
                // 유저 정보를 가져오지 못한 경우
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("유저 정보를 가져올 수 없습니다.");
            }

            // 필드 존재 여부에 따라 값 할당
            String kakaoId = userInfo.has("id") ? userInfo.get("id").getAsString() : null;
            String email = userInfo.has("email") ? userInfo.get("email").getAsString() : (kakaoId != null ? kakaoId + "@kakao.com" : null);
            String nickname = userInfo.has("nickname") ? userInfo.get("nickname").getAsString() : null;

            // 이메일과 loginType으로 사용자 존재 여부 확인
            boolean existingUser = userService.findByEmailAndLoginType(email, "kakao");
            log.info("existingUser : {}", existingUser);
            // 사용자가 존재하지 않을 경우 (회원가입 필요)
            if (!existingUser) {
                // UserForm 객체 생성 및 값 설정
                UserDto kakaoUser = new UserDto();
                kakaoUser.setName(nickname);
                kakaoUser.setUserLoginId(kakaoId);
                kakaoUser.setPassword("kakao"); // 소셜 로그인 사용자의 비밀번호는 'kakao'로 설정 (별도 처리 필요)
                kakaoUser.setEmail(email);
                kakaoUser.setUserLoginType("kakao");

                log.info("Kakao User 정보: {}", kakaoUser);

                // existingUser = false와 UserForm 객체를 프론트로 전달
                return ResponseEntity.ok().body(Map.of(
                        "existingUser", false,
                        "user", kakaoUser
                ));
            }

            // 사용자가 존재할 경우 로그인 처리
            else {
//                LoginForm loginForm = new LoginForm();
//                loginForm.setUserLoginId(kakaoId); // Kakao 로그인 시 사용자 ID로 email 사용
//                loginForm.setPassword("kakao"); // 소셜 로그인은 별도의 비밀번호 처리가 필요 (고정된 비밀번호 사용)
                // 로그인 처리
                Long userId = userService.findUserIdByKakaoId(kakaoId);
                userService.deleteRefreshTokenBeforeLogin(userId);
                String token = jwtTokenProvider.generateToken(userId);
                String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

                log.info("JWT 토큰 생성: {}", token);
                log.info("JWT REFERSH 토큰 생성 : {}", refreshToken);
                log.info("userId : {}", userId);
                // existingUser = true와 JWT 토큰을 프론트로 전달
                return ResponseEntity.ok().body(Map.of(
                        "existingUser", true,
                        "token", token,
                        "userId", userId,
                        "accessToken", accessToken,
                        "refreshToken", refreshToken
                ));
            }

        } catch (Exception e) {
            log.error("카카오 로그인 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카카오 로그인 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/social-kakao-logout")
    public ResponseEntity<?> kakaoLogout(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // "Bearer " 이후의 실제 토큰 부분 추출
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("token is null");
            }
            log.info("로그아웃 토큰 : {}", token);
            Long result = kakaoLoginService.kakaoLogout(token);

            log.info("result : {}", result);
            if (result != -1L) {
                userService.removeRefreshToken(request.getHeader("refreshToken"));
                return ResponseEntity.ok("kakao Logout Success");
            } else return ResponseEntity.status(HttpStatus.NOT_FOUND).body("kakao logout error");
        } catch (Exception e) {
            log.error("카카오 로그인 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("kakao logout error");
        }
    }

    @GetMapping("/terms")
    public ResponseEntity<List<UtilVo>> getTerms() {
        try {
            List<UtilVo> terms = userService.getUtilTerms();
            return ResponseEntity.ok(terms);
        } catch (IllegalArgumentException e) {
            log.info("잘못된 요청 : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);  // 400 Bad Request
        } catch (Exception e) {
            log.error("서버 에러 발생 : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);  // 500 Internal Server Error
        }
    }


//    @GetMapping("/social-signup/info")
//    public ResponseEntity<String> infoSocialSignup() {
//        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
//    }
//
//    @PostMapping("/social-signup/register")
//    public ResponseEntity<String> registerSocialSignup() {
//        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
//    }
//
//    @GetMapping("/test")
//    public String test() {
//        // user/index.jsp 페이지로 이동
//        return "/index";
//    }

}

