package beBig.controller;

import beBig.dto.*;
import beBig.service.HomeService;
import beBig.service.jwt.JwtUtil;
import beBig.vo.AccountVo;
import beBig.vo.UserVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    // 유저 정보 불러오기
    @GetMapping("/info")
    public ResponseEntity<HashMap<String, Object>> getMyInfo(@RequestHeader("Authorization") String token) {
        try {
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
        } catch (Exception e) {
            log.error("유저 정보 불러오기 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Internal Server Error
        }
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

    // 사용자 계좌 정보 가져오기 - codef
    @PostMapping("/account")
    public ResponseEntity<?> getAccount(@RequestHeader("Authorization") String token,
                                        @RequestBody AccountRequestDto accountRequestDto) {
        try {
            Long userId = jwtUtil.extractUserIdFromToken(token);
            List<CodefAccountDto> accountList = homeService.getUserAccount(userId, accountRequestDto);

            if (accountList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("등록된 계좌가 없습니다.");
            }

            return ResponseEntity.ok(accountList);
        } catch (Exception e) {
            log.error("계좌 정보 불러오기 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("계좌 정보 불러오기 중 오류가 발생했습니다.");
        }
    }


    // 계좌를 db에 등록
    @PostMapping("/account/add")
    public ResponseEntity<?> addAccount(@RequestHeader("Authorization") String token,
                                        @RequestBody List<CodefAccountDto> codefAccountDtoList) {
        try {
            Long userId = jwtUtil.extractUserIdFromToken(token);
            boolean isAdded = homeService.addAccountToDB(userId, codefAccountDtoList);

            if (isAdded) {
                return ResponseEntity.ok("계좌 추가 완료");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("계좌 추가 실패");
            }
        } catch (Exception e) {
            log.error("계좌 추가 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("계좌 추가 중 오류가 발생했습니다.");
        }
    }


//    @GetMapping("/mission")
//    public ResponseEntity<String> missionList(@RequestHeader("Authorization") String token) {
//        Long userNo = extractUserNoFromToken(token);
//        return ResponseEntity.status(HttpStatus.OK).body("미션 목록 조회");
//    }

    // 계좌 목록 불러오기
    @GetMapping("/account/list")
    public ResponseEntity<?> accountList(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserIdFromToken(token);
            List<AccountResponseDto> accountList = homeService.showMyAccount(userId);

            if (accountList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("등록된 계좌가 없습니다.");
            }

            return ResponseEntity.ok(accountList);
        } catch (Exception e) {
            log.error("계좌 목록 불러오기 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("계좌 목록 불러오기 중 오류가 발생했습니다.");
        }
    }
    // 계좌별 거래내역 조회
    @GetMapping("/account/{accountNum}/transactions")
    public ResponseEntity<?> getTransactionList(@RequestHeader("Authorization") String token,
                                                @PathVariable String accountNum) {
        try {
            Long userId = jwtUtil.extractUserIdFromToken(token);
            AccountTransactionDto response = homeService.getTransactionList(userId, accountNum);

            if (response == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("거래 내역을 찾을 수 없습니다.");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("거래 내역 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("거래 내역 조회 중 오류가 발생했습니다.");
        }
    }

    // 거래 내역 저장 -> Spring Batch를 활용하여 스케줄링 예정으로, 수정 필요함
    @PostMapping("/transactions")
    public ResponseEntity<?> saveTransactions(@RequestHeader("Authorization") String token,
                                              @RequestBody Map<String, String> requestBody) {
        try {
            Long userId = jwtUtil.extractUserIdFromToken(token);

            // requestBody에서 accountNum을 추출
            String accountNum = requestBody.get("accountNum");

            if (accountNum == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("계좌 번호가 누락되었습니다.");
            }

            // 추출한 accountNum을 사용하여 서비스 메서드 호출
            boolean isSaved = homeService.saveTransactions(userId, accountNum);

            if (isSaved) {
                return ResponseEntity.ok("거래 내역이 성공적으로 저장되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("거래 내역 저장에 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("거래 내역 저장 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("거래 내역 저장 중 오류가 발생했습니다.");
        }
    }
}
