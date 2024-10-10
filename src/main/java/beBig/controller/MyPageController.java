package beBig.controller;

import beBig.dto.request.MyPageEditRequestDto;
import beBig.dto.response.MyPagePostResponseDto;
import beBig.dto.response.MyPageEditResponseDto;
import beBig.dto.response.TotalMissionResponseDto;
import beBig.dto.response.UserProfileResponseDto;
import beBig.mapper.MyPageMapper;
import beBig.service.MissionService;
import beBig.service.MyPageService;
import beBig.service.jwt.JwtUtil;
import beBig.vo.BadgeVo;
import com.amazonaws.Response;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static beBig.controller.MissionController.getRestDaysInCurrentMonth;

@CrossOrigin("*")
@Controller
@RequestMapping("/mypage")
@Slf4j
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;
    private final MissionService missionService;
    private final JwtUtil jwtUtil;
    private final MyPageMapper myPageMapper;

    @GetMapping("/info")
    public ResponseEntity<UserProfileResponseDto> getMypage(@RequestHeader("Authorization") String token) {
        try {
            long userId = jwtUtil.extractUserIdFromToken(token);
            beBig.dto.response.UserProfileResponseDto dto = myPageService.findProfileByUserId(userId);
            log.info("dto : {}", dto);
            if (dto != null) {
                return ResponseEntity.status(HttpStatus.OK).body(dto);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            log.info("에러 메시지: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/badge")
    public ResponseEntity<?> personalBadge(@RequestHeader("Authorization") String token) {
        try {
            List<BadgeVo> badges = myPageService.getBadges();
            return ResponseEntity.status(HttpStatus.OK).body(badges);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("internal server error");
        }
    }

    @GetMapping("/mission")
    public ResponseEntity<?> monthlyMissionTotal(@RequestHeader("Authorization") String token) {
        try {
            long userId = jwtUtil.extractUserIdFromToken(token);
            int restDays = getRestDaysInCurrentMonth();
            int currentScore = missionService.findCurrentMonthScore(userId);
            TotalMissionResponseDto responseDto = new TotalMissionResponseDto(restDays, currentScore);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json; charset=UTF-8");
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(responseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the request.");
        }
    }

    @GetMapping("/posts")
    public ResponseEntity<List<MyPagePostResponseDto>> myPosts(@RequestHeader("Authorization") String token) {
        try {
            long userId = jwtUtil.extractUserIdFromToken(token);
            List<MyPagePostResponseDto> dto = myPageService.findMyPostByUserId(userId);
            return ResponseEntity.status(HttpStatus.OK).body(dto);
        } catch (Exception e) {
            log.error("게시물 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/posts/{userId}")
    public ResponseEntity<List<MyPagePostResponseDto>> myPostsByUserId(@PathVariable long userId) {
        try {
            List<MyPagePostResponseDto> dto = myPageService.findMyPostByUserId(userId);
            return ResponseEntity.status(HttpStatus.OK).body(dto);
        } catch (Exception e) {
            log.error("게시물 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/mylikehits")
    public ResponseEntity<List<MyPagePostResponseDto>> myLikeHits(@RequestHeader("Authorization") String token) {
        try {
            long userId = jwtUtil.extractUserIdFromToken(token);
            List<MyPagePostResponseDto> dto = myPageService.findMyLikeHitsByUserId(userId);
            return ResponseEntity.status(HttpStatus.OK).body(dto);
        } catch (Exception e) {
            log.error("좋아요 수 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/mylikehits/{userId}")
    public ResponseEntity<List<MyPagePostResponseDto>> myLikeHitsByUserId(@PathVariable long userId) {
        try {
            List<MyPagePostResponseDto> dto = myPageService.findMyLikeHitsByUserId(userId);
            return ResponseEntity.status(HttpStatus.OK).body(dto);
        } catch (Exception e) {
            log.error("좋아요 수 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/login-type")
    public ResponseEntity<String> checkLoginType(@RequestHeader("Authorization") String token) {
        try {
            long userId = jwtUtil.extractUserIdFromToken(token);
            String loginType = myPageService.findLoginIdByUserId(userId);
            log.info("loginType : {}", loginType);
            return ResponseEntity.ok(loginType);
        } catch (Exception e) {
            log.error("서버 에러 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버에서 오류가 발생했습니다.");
        }
    }

    @GetMapping("/edit-form")
    public ResponseEntity<MyPageEditResponseDto> editForm(@RequestHeader("Authorization") String token) {
        try {
            long userId = jwtUtil.extractUserIdFromToken(token);

            MyPageEditResponseDto dto = myPageService.findEditFormByUserId(userId);
            // 예시 숫자
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = dateFormat.format(dto.getUserBirth());
            dto.setUserBirth2(formattedDate);

            log.info("dto : {}", dto);
            return ResponseEntity.status(HttpStatus.OK).body(dto);
        } catch (Exception e) {
            log.error("마이페이지 수정 에러 발생 : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/edit-save")
    public ResponseEntity<String> editSave(@RequestHeader("Authorization") String token,
                                           @RequestBody Map<String, Object> requestBody) {
        try {
            long userId = jwtUtil.extractUserIdFromToken(token);
            String userIntro = (String) requestBody.get("user_intro");
            String userNickname = (String) requestBody.get("user_nickname");
            String loginType = (String) requestBody.get("user_login_type");

            if (loginType.equals("kakao")) {
                myPageService.saveMyPageSocial(userId, userIntro, userNickname);
            } else {
                String password = (String) requestBody.get("user_password");

                // 비밀번호가 존재할 때만 업데이트
                if (password != null && !password.isEmpty()) {
                    myPageService.saveMyPageGeneral(userId, userIntro, userNickname, password);
                } else {
                    // 비밀번호가 없으면 비밀번호를 제외하고 업데이트
                    myPageService.saveMyPageGeneralWithoutPassword(userId, userIntro, userNickname);
                }
            }
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            log.error("서버 에러 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("server error.");
        }
    }

    @PostMapping("/check-password")
    public ResponseEntity<?> checkPassword(@RequestHeader("Authorization") String token,
                                           @RequestBody Map<String, Object> requestBody) {
        try {
            long userId = jwtUtil.extractUserIdFromToken(token);
            String password = (String) requestBody.get("password");

            if (!myPageService.checkPassword(password, userId)) {
                return ResponseEntity.status(HttpStatus.OK).body("password does not match");
            }
            return ResponseEntity.ok("password match");
        } catch (Exception e) {
            log.error("서버 에러 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("server error.");
        }
    }

    //로그인된 사용자의 userId확인 하는 컨트롤러
    @GetMapping("/logged-in-user-id")
    public ResponseEntity<Long> getLoggedInUserId(@RequestHeader("Authorization") String token) {
        try {
            long userId = jwtUtil.extractUserIdFromToken(token);
            log.info("Extracted userId: {}", userId);  // 추가 로그
            return ResponseEntity.ok(userId);
        } catch (Exception e) {
            log.error("logged-in-user-id: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 공개, 비공개 바꿔주는 컨트롤러
    @PostMapping("/update-visibility")
    public ResponseEntity<String> updateVisibility(@RequestHeader("Authorization") String token,
                                                   @RequestBody Map<String, Object> requestBody) {


        try {
            long userId = jwtUtil.extractUserIdFromToken(token);

            // visibility 값을 정수형으로 변환
            Integer visibility = (Integer) requestBody.get("visibility");
            if (visibility == null) {
                log.error("Visibility 값이 null입니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Visibility 값이 누락되었습니다.");
            }

            myPageService.updateVisibility(userId, visibility);  // 서비스에서 상태 업데이트
            return ResponseEntity.ok("Visibility updated successfully");
        } catch (Exception e) {
            log.error("Error updating visibility", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update visibility");
        }
    }

    // 특정 사용자의 프로필을 조회하는 엔드포인트
    @GetMapping("/info/{userId}")
    public ResponseEntity<UserProfileResponseDto> getMypageByUserId(@PathVariable long userId) {
        try {
            UserProfileResponseDto dto = myPageService.findProfileByUserId(userId);
            log.info("/info/{userId} dto : {}", dto);
            if (dto != null) {
                return ResponseEntity.status(HttpStatus.OK).body(dto);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            log.info("에러 메시지: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
