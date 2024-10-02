package beBig.controller;

import beBig.dto.response.FinInfoResponseDto;
import beBig.service.HomeService;
import beBig.service.jwt.JwtTokenProvider;
import beBig.service.jwt.JwtUtil;
import beBig.vo.AccountVo;
import beBig.vo.FinTestVo;
import beBig.vo.UserVo;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@Controller
@RequestMapping("/home")
@Slf4j
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;
    private final JwtUtil jwtUtil; // JWT 토큰 제공자 주입
    private final JwtTokenProvider jwtTokenProvider;

    // 유저 정보 불러오기
    // ******수정 필요******
    @GetMapping("/info")
    public ResponseEntity<HashMap<String, Object>> getMyInfo(@RequestHeader("Authorization") String token) throws Exception {
        Long userId = jwtUtil.extractUserIdFromToken(token);

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

    @GetMapping("/fin-test")
    public ResponseEntity<List<FinTestVo>> getTest() {
        try {
            List<FinTestVo> testList = homeService.findMission();
            return ResponseEntity.ok(testList);
        } catch (IllegalArgumentException e) {
            log.info("잘못된 요청 : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);  // 400 Bad Request
        } catch (Exception e) {
            log.error("서버 에러 발생 : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);  // 500 Internal Server Error
        }
    }

    @GetMapping("/fin-type")
    public ResponseEntity<FinInfoResponseDto> getType(@RequestHeader("Authorization") String token) {
        try {
            // JWT 토큰에서 userId 추출
            Long userId = jwtUtil.extractUserIdFromToken(token);
            // 추출된 userId로 금융 정보를 가져옴
            FinInfoResponseDto type = homeService.findFinTypeByUserId(userId);
            return ResponseEntity.ok(type);
        } catch (JwtException e) {
            log.error("JWT 토큰 처리 중 에러 발생 : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);  // 401 Unauthorized
        } catch (IllegalArgumentException e) {
            log.error("잘못된 요청 : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);  // 400 Bad Request
        } catch (Exception e) {
            log.error("서버 에러 발생 : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);  // 500 Internal Server Error
        }
    }

    @PostMapping("/fin-save")
    public ResponseEntity<String> saveFinType(@RequestHeader("Authorization") String token,
                                              @RequestBody Map<String, Object> requestBody) {
        try {
            Long userId = jwtUtil.extractUserIdFromToken(token);
            Integer userFinType = (Integer) requestBody.get("user_fin_type");
            if (userFinType == null) {
                log.error("user_fin_type 값이 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user_fin_type 값이 필요합니다.");
            }
            log.info("userFinType: {}", userFinType);
            homeService.saveUserFinType(userId, userFinType);

            return ResponseEntity.ok("success");
        } catch (JwtException e) {
            log.error("JWT 토큰 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        } catch (ClassCastException e) {
            log.error("user_fin_type 값 타입 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user_fin_type 값은 정수형이어야 합니다.");
        } catch (Exception e) {
            log.error("서버 에러 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버에서 오류가 발생했습니다.");
        }
    }


    //
//    @GetMapping("/account/{accountNum}/detail")
//    public ResponseEntity<String> transactionList(@PathVariable String accountNum,
//                                                  @RequestHeader("Authorization") String token) {
//        Long userNo = extractUserNoFromToken(token);
//        return ResponseEntity.status(HttpStatus.OK).body("거래 내역 조회");
//    }
}