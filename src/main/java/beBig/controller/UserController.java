package beBig.controller;

import beBig.form.LoginForm;
import beBig.form.UserForm;
import beBig.service.CustomUserDetails;
import beBig.service.CustomUserDetailsService;
import beBig.service.UserService;
import beBig.service.jwt.JwtTokenProvider;
import beBig.service.oauth.KakaoOauthService;
import beBig.vo.UtilVo;
import com.google.gson.JsonObject;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

//    @Autowired
//    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager,
//                          CustomUserDetailsService customUserDetailsService, KakaoOauthService kakaoLoginService) {
//        this.userService = userService;
//        this.jwtTokenProvider = jwtTokenProvider;
//        this.authenticationManager = authenticationManager;
//        this.customUserDetailsService = customUserDetailsService;
//        this.kakaoLoginService = kakaoLoginService;
//    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserForm userForm) {
        log.info("회원가입 요청: {}", userForm);
        try {
            // 필수 필드 확인 예시 (예: 이메일이 없으면 에러 반환)
            if (userForm.getEmail() == null || userForm.getEmail().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이메일은 필수 항목입니다.");
            }
            // 사용자 등록 로직 호출
            userService.registerUser(userForm);
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
    public ResponseEntity<String> login(@RequestBody LoginForm loginForm) {
        try {
            // 사용자 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginForm.getUserLoginId(),
                            loginForm.getPassword()
                    )
            );

            log.info("Received login request: {}", loginForm);

            // 인증된 사용자 정보 로드
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.info("로그인 성공: " + loginForm);

            // JWT 토큰 생성
            String token = jwtTokenProvider.generateToken(userDetails.getUserId());
            log.info("JWT 토큰 생성: " + token);

            // 토큰을 클라이언트로 응답
            return ResponseEntity.status(HttpStatus.OK).body(token);

        } catch (BadCredentialsException e) {
            log.error("로그인 실패: " + loginForm);
            log.error("Bad credentials: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패!");
        } catch (AuthenticationException e) {
            log.error("로그인 실패: " + loginForm);
            log.error("Authentication exception: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패!");
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

        return ResponseEntity.status(HttpStatus.OK).body("로그아웃에 성공하였습니다!");
    }

    @PostMapping("/find-id")
    public ResponseEntity<String> findUserId(@RequestBody UserForm userForm) {
        String name = userForm.getName();
        String email = userForm.getEmail();

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
    public ResponseEntity<String> findPassword(@RequestBody UserForm userForm) {
        boolean isUpdated = userService.updatePasswordByEmail(userForm.getUserLoginId(), userForm.getName(), userForm.getEmail());

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
                UserForm kakaoUser = new UserForm();
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
                String token = jwtTokenProvider.generateToken(Long.valueOf(kakaoId));
                log.info("JWT 토큰 생성: {}", token);

                // existingUser = true와 JWT 토큰을 프론트로 전달
                return ResponseEntity.ok().body(Map.of(
                        "existingUser", true,
                        "token", token,
                        "userId", kakaoId
                ));
            }

        } catch (Exception e) {
            log.error("카카오 로그인 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카카오 로그인 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/terms")
    public ResponseEntity<List<UtilVo>> getTerms() {
        List<UtilVo> terms = userService.getUtilTerms();
        return ResponseEntity.ok(terms);
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
