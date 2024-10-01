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

    // 사용자 계좌 정보 가져오기 - codef
    @PostMapping("/account")
    public ResponseEntity<List<CodefAccountDto>> getAccount(@RequestHeader("Authorization") String token,
                                                            @RequestBody AccountRequestDto accountRequestDto) throws Exception {
        Long userId = jwtUtil.extractUserIdFromToken(token);
        List<CodefAccountDto> accountList = homeService.getUserAccount(userId, accountRequestDto);
        return ResponseEntity.ok(accountList);
    }

    // 계좌를 db에 등록
    @PostMapping("/account/add")
    public ResponseEntity<String> addAccount(@RequestHeader("Authorization") String token,
                                             @RequestBody List<CodefAccountDto> codefAccountDtoList) throws Exception {
        Long userId = jwtUtil.extractUserIdFromToken(token);
        boolean isAdded = homeService.addAccountToDB(userId, codefAccountDtoList);
        return isAdded ? ResponseEntity.ok("계좌 추가 완료") : ResponseEntity.badRequest().body("계좌 추가 실패");
    }


//    @GetMapping("/mission")
//    public ResponseEntity<String> missionList(@RequestHeader("Authorization") String token) {
//        Long userNo = extractUserNoFromToken(token);
//        return ResponseEntity.status(HttpStatus.OK).body("미션 목록 조회");
//    }

    //
    // 계좌 목록 불러오기
    @GetMapping("/account/list")
    public ResponseEntity<List<AccountResponseDto>> accountList(@RequestHeader("Authorization") String token) throws Exception {
        Long userId = jwtUtil.extractUserIdFromToken(token);
        List<AccountResponseDto> accountList = homeService.showMyAccount(userId);

        return ResponseEntity.ok(accountList);
    }

    // 계좌별 거래내역 조회
    @GetMapping("/account/{accountNum}/transactions")
    public ResponseEntity<AccountTransactionDto> getTransactionList(@RequestHeader("Authorization") String token,
                                                                    @PathVariable String accountNum) {
        Long userId = jwtUtil.extractUserIdFromToken(token);
        AccountTransactionDto response = homeService.getTransactionList(userId, accountNum);
        return ResponseEntity.ok(response);
    }

    // 거래 내역 저장 -> Spring Batch를 활용하여 스케줄링 예정으로, 수정 필요함
    @PostMapping("/transactions")
    public ResponseEntity<Void> saveTransactions(@RequestHeader("Authorization") String token,
                                                 @RequestBody Map<String, String> requestBody) throws Exception {
        Long userId = jwtUtil.extractUserIdFromToken(token);

        // 거래 내역 요청 객체 생성 및 설정
        CodefTransactionRequestDto requestDto = new CodefTransactionRequestDto();
        UserVo userInfo = homeService.getUserInfo(userId);
        requestDto.setAccount(requestBody.get("accountNum"));
        requestDto.setConnectedId(userInfo.getUserConnectedId());
        requestDto.setOrganization("0004");

        // 날짜 설정 (최근 3일)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(3);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        requestDto.setStartDate(startDate.format(formatter));
        requestDto.setEndDate(endDate.format(formatter));
        requestDto.setOrderBy("0");

        // 거래 내역 저장
        homeService.saveTransactions(userId, requestDto);

        return ResponseEntity.ok().build();
    }
}