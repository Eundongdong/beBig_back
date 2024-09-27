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

import java.awt.event.WindowFocusListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@Controller
@RequestMapping("/mission")
@Slf4j
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @GetMapping("/{userId}/monthly")
    public ResponseEntity<String> monthlyMission(@PathVariable Long userId) {
        MonthlyMissionResponseDto dto = missionService.showMonthlyMission(userId);
        log.info(dto.toString());
        return ResponseEntity.status(HttpStatus.OK).body(dto.toString());
    }

    @GetMapping("/{userId}/daily")
    public ResponseEntity<String> dailyMission(@PathVariable Long userId) {
        List<DailyMissionResponseDto> dto = missionService.showDailyMission(userId);
        log.info(dto.toString());
        return ResponseEntity.status(HttpStatus.OK).body(dto.toString());
    }

    @PostMapping("/{userId}/complete")
    public ResponseEntity<String> completeMission(@PathVariable Long userId,
                                                  @RequestParam Long personalMissionId,
                                                  @RequestParam int missionType) {
        if(missionType == 1) missionService.completeMonthlyMission(personalMissionId);
        else if(missionType == 2) missionService.completeDailyMission(personalMissionId);
        return ResponseEntity.status(HttpStatus.OK).body("complete sucess");
    }

}
