package beBig.controller;

import beBig.dto.*;
import beBig.dto.response.FinInfoResponseDto;
import beBig.service.HomeService;
import beBig.service.jwt.JwtUtil;
import beBig.vo.FinTestVo;
import beBig.vo.UserVo;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/account")
    public ResponseEntity<?> getAccount(@RequestHeader("Authorization") String token,
                                        @RequestBody AccountRequestDto accountRequestDto) {
        try {
            log.info(accountRequestDto.toString());
            Long userId = jwtUtil.extractUserIdFromToken(token);
            List<CodefAccountDto> accountList = homeService.getUserAccount(userId, accountRequestDto);
            log.info(accountList.toString());

            if (accountList.size() == 0 || accountList == null) {
                log.info("등록된 계좌가 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("아이디/비밀번호를 확인하세요.");
            }

            return ResponseEntity.ok(accountList);
        } catch (Exception e) {
            // 에러 발생 시 메시지를 클라이언트로 전달
            log.error("Error occurred: ", e);  // 에러 로그 출력
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
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
    // batch를 통한 구현 완료 -> 이 부분은 사용자가 새로고침 버튼을 누르면 직접 호출하는 기능으로 활용하거나, 삭제
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
}
