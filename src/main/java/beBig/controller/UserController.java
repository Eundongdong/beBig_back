package beBig.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {
    @PostMapping("/signup")
    public ResponseEntity<String> signup() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @GetMapping("/login/{userId}")
    public ResponseEntity<String> idDuplicateCheck(@PathVariable Long userId) {
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
    public ResponseEntity<String> findUserId() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @PostMapping("/find-pwd")
    public ResponseEntity<String> findUserPwd() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }
}
