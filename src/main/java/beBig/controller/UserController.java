package beBig.controller;

import beBig.form.UserForm;
import beBig.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@ModelAttribute UserForm userForm) throws Exception {
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

    @GetMapping("/social-signup/info")
    public ResponseEntity<String> infoSocialSignup() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @PostMapping("/social-signup/register")
    public ResponseEntity<String> registerSocialSignup() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @PostMapping("/social-login")
    public ResponseEntity<String> socialLogin() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @PostMapping("/find-id")
    public ResponseEntity<String> findUserId(@RequestParam String name, @RequestParam String email) {
        log.info("Received name: {}, email: {}", name, email);
        String maskedUserId = userService.findUserIdByNameAndEmail(name, email);

        if (maskedUserId != null) {
            log.info("Found user id: {}", maskedUserId);
            return ResponseEntity.status(HttpStatus.OK).body("아이디는: " + maskedUserId);
        } else {
            log.info("No user found with name: {}", name);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 이름과 이메일로 등록된 아이디가 없습니다.");
        }
    }

    @PostMapping("/find-pwd")
    public ResponseEntity<String> findUserPwd() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

}
