package beBig.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@Controller
@RequestMapping("/mypage")
@Slf4j
public class MyPageController {
    @GetMapping("/{userNo}/info")
    public ResponseEntity<String> getMypage(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @GetMapping("/{userNo}/badge")
    public ResponseEntity<String> badge(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @GetMapping("/{userNo}/mission-achievement")
    public ResponseEntity<String> missionAchievement(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @GetMapping("/{userNo}/myposts")
    public ResponseEntity<String> myposts(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @GetMapping("/{userNo}/mylikehits")
    public ResponseEntity<String> myLikeHits(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @PutMapping("/{userNo}/edit")
    public ResponseEntity<String> edit(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @PutMapping("/{userNo}/social-edit")
    public ResponseEntity<String> socialEdit(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }
}
