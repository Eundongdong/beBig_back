package beBig.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@Controller
@RequestMapping("/home")
@Slf4j
public class HomeController {

    @GetMapping("/{userNo}/info")
    public ResponseEntity<String> getMyInfo(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @GetMapping("/{userNo}/survey")
    public ResponseEntity<String> surveyList(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @PostMapping("/{userNo}/survey")
    public ResponseEntity<String> submitSurvey(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @GetMapping("/{userNo}/survey-result")
    public ResponseEntity<String> surveyResult(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @PostMapping("/{userNo}/account/add")
    public ResponseEntity<String> addAccount(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @GetMapping("/{userNo}/mission")
    public ResponseEntity<String> missionList(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @GetMapping("/{userNo}/account/list")
    public ResponseEntity<String> accountList(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @GetMapping("/{userNo}/account/{accountNum}/detail")
    public ResponseEntity<String> transactionList(@PathVariable Long userNo,
                                                  @PathVariable String accountNum) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

}