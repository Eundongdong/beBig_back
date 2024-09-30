package beBig.controller;

import beBig.dto.response.DailyMissionResponseDto;
import beBig.dto.response.MonthlyMissionResponseDto;
import beBig.dto.response.TotalMissionResponseDto;
import beBig.service.MissionService;
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

    @GetMapping("/{userId}/total")
    public ResponseEntity<?> monthlyMissionTotal(@PathVariable long userId) {
        try {
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


    @GetMapping("/{userId}/monthly")
    public ResponseEntity<?> monthlyMission(@PathVariable Long userId) {
        try {
            MonthlyMissionResponseDto dto = missionService.showMonthlyMission(userId);
            int salary = missionService.findSalary(userId);
            double rate = missionService.findRate(userId, dto.getMissionId());
            dto.setMissionTopic(replaceNWithNumber(dto.getMissionTopic(), salary, rate));
            log.info("사용자 ID: {}의 월간 미션 조회 성공: {}", userId, dto);
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
                //미션타입 확인하고 점수계산 - 일간
                int totalDays = getDaysInCurrentMonth();
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
            log.error("사용자 ID: {}의 미션 완료 중 오류 발생: 미션 ID: {}", userId, personalMissionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurs During mission complete.");
        }
    }

    //날짜관련 계산

    // N 존재하는지 판별하고 'n'을 숫자로 변환
    public String replaceNWithNumber(String s, int number, double rate) {
        StringBuilder result = new StringBuilder();
        int calSalary = (int) (number * (rate / 100));

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == 'n') {
                result.append(calSalary);
            } else {
                result.append(s.charAt(i));
            }
        }
        return result.toString();
    }

    // 오늘로부터 이번 달이 끝날 때까지 남은 일수를 반환하는 메서드
    public static int getRestDaysInCurrentMonth() {
        LocalDate today = LocalDate.now();
        LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        return (int) ChronoUnit.DAYS.between(today, lastDayOfMonth);
    }

    // 이번 달의 총 일수를 반환하는 메서드
    public static int getDaysInCurrentMonth() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        return lastDayOfMonth.getDayOfMonth() - firstDayOfMonth.getDayOfMonth() + 1;
    }
}
