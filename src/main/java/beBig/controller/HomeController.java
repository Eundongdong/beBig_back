package beBig.controller;

import beBig.service.HomeService;
import beBig.service.jwt.JwtTokenProvider;
import beBig.service.jwt.JwtUtil;
import beBig.vo.AccountVo;
import beBig.vo.UserVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@CrossOrigin("*")
@Controller
@RequestMapping("/home")
@Slf4j
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;
    private final JwtUtil jwtUtil; // JWT 토큰 제공자 주입

    // 유저 정보 불러오기
    // ******수정 필요******
    @GetMapping("/info")
    public ResponseEntity<HashMap<String, Object>> getMyInfo(@RequestHeader("Authorization") String token) throws Exception {
        Long userId = jwtUtil.extractUserIdFromToken(token);
        UserVo userInfo = homeService.getUserInfo(userId);

        if (userInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Not Found
        }

        HashMap<String, Object> response = new HashMap<>();
        response.put("userName", userInfo.getUserName());
        response.put("finType", userInfo.getFinType());
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


    @PostMapping("/account/add")
    public ResponseEntity<String> addAccount(@RequestHeader("Authorization") String token) {
        return ResponseEntity.status(HttpStatus.OK).body("계좌 추가 완료");
    }
//
//    @GetMapping("/mission")
//    public ResponseEntity<String> missionList(@RequestHeader("Authorization") String token) {
//        Long userNo = extractUserNoFromToken(token);
//        return ResponseEntity.status(HttpStatus.OK).body("미션 목록 조회");
//    }
//
// 계좌 목록 불러오기
@GetMapping("/account/list")
public ResponseEntity<List<AccountVo>> accountList(@RequestHeader("Authorization") String token) throws Exception {

    Long userId = jwtUtil.extractUserIdFromToken(token);
    List<AccountVo> accountList = homeService.showMyAccount(userId);

    return ResponseEntity.ok(accountList);
}

//
//    @GetMapping("/account/{accountNum}/detail")
//    public ResponseEntity<String> transactionList(@PathVariable String accountNum,
//                                                  @RequestHeader("Authorization") String token) {
//        Long userNo = extractUserNoFromToken(token);
//        return ResponseEntity.status(HttpStatus.OK).body("거래 내역 조회");
//    }
}