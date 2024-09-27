package beBig.controller;

import beBig.service.HomeService;
import beBig.service.jwt.JwtTokenProvider;
import beBig.vo.UserVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@CrossOrigin("*")
@Controller
@RequestMapping("/home")
@Slf4j
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;
    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 제공자 주입

    @GetMapping("/info")
    public ResponseEntity<HashMap<String, Object>> getMyInfo(@RequestHeader("Authorization") String authorizationHeader) throws Exception {
        // Authorization 헤더에서 JWT 토큰 추출
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Unauthorized
        }

        String token = authorizationHeader.substring(7); // "Bearer " 이후 부분 추출

        // JWT 토큰 검증 : 추후 토큰 처리 관련 일괄 수정 필요
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Unauthorized
        }

        Long userId = jwtTokenProvider.getUserIdFromJWT(token); // 사용자 이름 추출
        UserVo userInfo = homeService.getUserInfo(userId);

        if (userInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Not Found
        }

        log.info("사용자 finTypeCode: {}", userInfo.getFinTypeCode());


        HashMap<String, Object> response = new HashMap<>();
        response.put("userName", userInfo.getUserName());
        response.put("finTypeCode", userInfo.getFinTypeCode());
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/survey")
//    public ResponseEntity<String> surveyList(@RequestHeader("Authorization") String token) {
//        Long userNo = extractUserNoFromToken(token);
//        return ResponseEntity.status(HttpStatus.OK).body("설문 목록");
//    }
//
//    @PostMapping("/survey")
//    public ResponseEntity<String> submitSurvey(@RequestHeader("Authorization") String token) {
//        Long userNo = extractUserNoFromToken(token);
//        return ResponseEntity.status(HttpStatus.OK).body("설문 제출 완료");
//    }
//
//    @GetMapping("/survey-result")
//    public ResponseEntity<String> surveyResult(@RequestHeader("Authorization") String token) {
//        Long userNo = extractUserNoFromToken(token);
//        return ResponseEntity.status(HttpStatus.OK).body("설문 결과 조회");
//    }
//
//    @PostMapping("/account/add")
//    public ResponseEntity<String> addAccount(@RequestHeader("Authorization") String token) {
//        Long userNo = extractUserNoFromToken(token);
//        return ResponseEntity.status(HttpStatus.OK).body("계좌 추가 완료");
//    }
//
//    @GetMapping("/mission")
//    public ResponseEntity<String> missionList(@RequestHeader("Authorization") String token) {
//        Long userNo = extractUserNoFromToken(token);
//        return ResponseEntity.status(HttpStatus.OK).body("미션 목록 조회");
//    }
//
//    @GetMapping("/account/list")
//    public ResponseEntity<String> accountList(@RequestHeader("Authorization") String token) {
//        Long userNo = extractUserNoFromToken(token);
//        return ResponseEntity.status(HttpStatus.OK).body("계좌 목록 조회");
//    }
//
//    @GetMapping("/account/{accountNum}/detail")
//    public ResponseEntity<String> transactionList(@PathVariable String accountNum,
//                                                  @RequestHeader("Authorization") String token) {
//        Long userNo = extractUserNoFromToken(token);
//        return ResponseEntity.status(HttpStatus.OK).body("거래 내역 조회");
//    }
}