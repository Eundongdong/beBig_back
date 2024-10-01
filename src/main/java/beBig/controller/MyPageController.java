package beBig.controller;

import beBig.dto.response.MyPagePostResponseDto;
import beBig.dto.response.TotalMissionResponseDto;
import beBig.dto.response.UserProfileResponseDto;
import beBig.service.MissionService;
import beBig.vo.UserProfileResponseVo;
import beBig.service.MyPageService;
import beBig.service.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

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

}
