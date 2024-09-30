package beBig.controller;

import beBig.dto.response.DailyMissionResponseDto;
import beBig.dto.response.MonthlyMissionResponseDto;
import beBig.service.MissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@CrossOrigin("*")
@Controller
@RequestMapping("/mission")
@Slf4j
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @GetMapping("/{userId}/monthly")
    public ResponseEntity<?> monthlyMission(@PathVariable Long userId) {
        try {
            MonthlyMissionResponseDto dto = missionService.showMonthlyMission(userId);
            log.info("사용자 ID: {}의 월간 미션 조회 성공: {}", userId, dto);

            LocalDate today = LocalDate.now();  // 현재 날짜
            YearMonth currentMonth = YearMonth.from(today);  // 오늘 날짜가 속한 YearMonth 객체 생성
            int daysInMonth = currentMonth.lengthOfMonth();  // 해당 달의 일수를 가져옴
            log.info("오늘 날짜가 속한 달의 일수는: " + daysInMonth + "일입니다.");

            return ResponseEntity.status(HttpStatus.OK).body(dto);
        } catch (Exception e) {
            log.error("사용자 ID: {}의 월간 미션 조회 중 오류 발생", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Monthly mission check error occurs");
        }
    }

    @GetMapping("/{userId}/daily")
    public ResponseEntity<?> dailyMission(@PathVariable Long userId) {
        try {
            List<DailyMissionResponseDto> dto = missionService.showDailyMission(userId);
            log.info("사용자 ID: {}의 일일 미션 조회 성공: {}", userId, dto);
            return ResponseEntity.status(HttpStatus.OK).body(dto);
        } catch (Exception e) {
            log.error("사용자 ID: {}의 일일 미션 조회 중 오류 발생", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Daily mission check error occurs");
        }
    }

    @PostMapping("/{userId}/complete")
    public ResponseEntity<?> completeMission(@PathVariable Long userId,
                                             @RequestParam Long personalMissionId,
                                             @RequestParam int missionType) {
        try {
            if (missionType == 1) {
                missionService.completeMonthlyMission(personalMissionId);
                log.info("사용자 ID: {}의 월간 미션 변경: 미션 ID: {}", userId, personalMissionId);
            } else if (missionType == 2) {
                missionService.completeDailyMission(personalMissionId);
                log.info("사용자 ID: {}의 일일 미션 변경: 미션 ID: {}", userId, personalMissionId);
            } else {
                log.warn("잘못된 미션 타입: {} (사용자 ID: {})", missionType, userId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Wrong mission type error.");
            }
            return ResponseEntity.status(HttpStatus.OK).body("success");
        } catch (Exception e) {
            log.error("사용자 ID: {}의 미션 완료 중 오류 발생: 미션 ID: {}", userId, personalMissionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurs During mission complete.");
        }
    }
}
