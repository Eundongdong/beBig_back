package beBig.controller;

import beBig.dto.request.MissionCompleteRequestDto;
import beBig.dto.response.DailyMissionResponseDto;
import beBig.dto.response.MonthlyMissionResponseDto;
import beBig.dto.response.TotalMissionResponseDto;
import beBig.service.MissionService;
import beBig.service.UserServiceImp;
import beBig.service.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.protocol.HTTP;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@CrossOrigin("*")
@Controller
@RequestMapping("/mission")
@Slf4j
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;
    private final JwtUtil jwtUtil;
    private final UserServiceImp userService;

    @GetMapping("/total")
    public ResponseEntity<?> monthlyMissionTotal(@RequestHeader("Authorization") String token) {
        try {
            long userId = jwtUtil.extractUserIdFromToken(token);
            int restDays = missionService.getRestDaysInCurrentMonth();
            int currentScore = missionService.findCurrentMonthScore(userId);
            TotalMissionResponseDto responseDto = new TotalMissionResponseDto(restDays, currentScore);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json; charset=UTF-8");
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(responseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the request.");
        }
    }

    @GetMapping("/total/{userId}")
    public ResponseEntity<?> monthlyMissionTotalByUserId(@PathVariable long userId) {
        try {
            int restDays = missionService.getRestDaysInCurrentMonth();
            int currentScore = missionService.findCurrentMonthScore(userId);
            TotalMissionResponseDto responseDto = new TotalMissionResponseDto(restDays, currentScore);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json; charset=UTF-8");
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(responseDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the request.");
        }
    }


    @GetMapping("/monthly")
    public ResponseEntity<?> monthlyMission(@RequestHeader("Authorization") String token) {
        try {
            long userId = jwtUtil.extractUserIdFromToken(token);
            MonthlyMissionResponseDto dto = missionService.showMonthlyMission(userId);
            int salary = missionService.findSalary(userId);
            double rate = missionService.findRate(userId, dto.getMissionId());
            dto.setMissionTopic(missionService.replaceNWithNumber(dto.getMissionTopic(), salary, rate));
            log.info("사용자 ID: {}의 월간 미션 조회 성공: {}", userId, dto);
            return ResponseEntity.status(HttpStatus.OK).body(dto);

        } catch (Exception e) {
            log.error("월간 미션 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Monthly mission check error occurs");
        }
    }

    @GetMapping("/daily")
    public ResponseEntity<?> dailyMission(@RequestHeader("Authorization") String token) {
        try {
            long userId = jwtUtil.extractUserIdFromToken(token);
            List<DailyMissionResponseDto> dto = missionService.showDailyMission(userId);
            log.info("사용자 ID: {}의 일일 미션 조회 성공: {}", userId, dto);
            return ResponseEntity.status(HttpStatus.OK).body(dto);
        } catch (Exception e) {
            log.error("일간 미션 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Daily mission check error occurs");
        }
    }

    @PostMapping("/complete")
    public ResponseEntity<?> completeMission(@RequestHeader("Authorization") String token,
                                             @RequestBody MissionCompleteRequestDto requestDto) {
        try {
            long userId = jwtUtil.extractUserIdFromToken(token);
            int missionType = requestDto.getMissionType();
            long personalMissionId = requestDto.getPersonalMissionId();

            if (missionType == 1) {
                //미션타입 확인하고 점수계산 - 일간
                int totalDays = missionService.getDaysInCurrentMonth();
                missionService.updateScore(userId, 100 - (totalDays * 2));
                missionService.completeMonthlyMission(personalMissionId);
                //점수계산
                log.info("사용자 ID: {}의 월간 미션 변경: 미션 ID: {}", userId, personalMissionId);
            } else if (missionType == 2) {
                //미션타입 확인하고 점수계산 - 일간
                int amount = 2;
                long isCompleted = missionService.findIsCompleted(personalMissionId);
                if (isCompleted == 1) amount = -2;
                missionService.updateScore(userId, amount); // 값 변화
                missionService.completeDailyMission(personalMissionId); // 상태바꾸기
                log.info("사용자 ID: {}의 일일 미션 변경: 미션 ID: {}", userId, personalMissionId);
            } else {
                log.warn("잘못된 미션 타입: {} (사용자 ID: {})", missionType, userId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Wrong mission type error.");
            }
            return ResponseEntity.status(HttpStatus.OK).body("success");
        } catch (Exception e) {
            log.error("미션 완료 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurs During mission complete.");
        }
    }

    @PostMapping("/complete-monthly-mission")
    public ResponseEntity<?> completeMonthlyMission(@RequestHeader("Authorization") String token) {
        long userId = jwtUtil.extractUserIdFromToken(token);
        //missionId는 int로 설정되어있음
        //1.월간 미션 아이디 확인하기
        long missionId = missionService.getMonthlyMissionNumber(userId);
        log.info("missionId : {}", missionId);
        boolean isSuccess = missionService.hasMonthlyMissionSucceeded(missionId, userId);
        if (isSuccess) {
            log.info("성공");
        } else {
            log.info("실패띠");
        }
        return ResponseEntity.status(HttpStatus.OK).body("monthly_mission : " + isSuccess);
    }

}
