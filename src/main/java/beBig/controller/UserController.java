package beBig.controller;

import beBig.form.LoginForm;
import beBig.form.UserForm;
import beBig.service.CustomUserDetailsService;
import beBig.service.UserService;
import beBig.service.jwt.JwtTokenProvider;
import beBig.vo.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@CrossOrigin("*")
@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager, CustomUserDetailsService customUserDetailsService) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.customUserDetailsService = customUserDetailsService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserForm userForm) throws Exception {
        log.info(String.valueOf(userForm));
        userService.registerUser(userForm);
        return ResponseEntity.status(HttpStatus.OK).body("유저등록완료!");
    }

    @GetMapping("/login/{userId}")
    public ResponseEntity<String> idDuplicateCheck(@PathVariable String userId) {
        boolean isDuplicated = userService.isUserIdDuplicated(userId);
        if (isDuplicated) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("중복된 아이디입니다.");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body("사용 가능한 아이디입니다.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginForm loginForm) {
        try {
            // AuthenticationManager를 사용하여 사용자 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginForm.getUserId(),
                            loginForm.getPassword()
                    )
            );
            // 인증된 사용자 정보 로드
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            log.info("로그인 성공: " + loginForm);
            // JWT 토큰 생성
            String token = jwtTokenProvider.generateToken(userDetails.getUsername());
            log.info("JWT 토큰 생성: " + token);

            // 토큰을 클라이언트로 응답
            return ResponseEntity.status(HttpStatus.OK).body(token);

        } catch (AuthenticationException e) {
            log.error("로그인 실패: " + loginForm);
            log.error(e.getMessage());
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
        String maskedUserId = userService.findUserIdByNameAndEmail(name, email);

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
        boolean isUpdated = userService.updatePasswordByEmail(userForm.getUserId(), userForm.getName(), userForm.getEmail());

        if (isUpdated) {
            return ResponseEntity.status(HttpStatus.OK).body("Temporary password sent to your email!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    @PostMapping("/social-login")
    public ResponseEntity<String> socialLogin() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @GetMapping("/social-signup/info")
    public ResponseEntity<String> infoSocialSignup() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @PostMapping("/social-signup/register")
    public ResponseEntity<String> registerSocialSignup() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

}
