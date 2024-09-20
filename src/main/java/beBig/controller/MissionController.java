package beBig.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin("*")
@Controller
@RequestMapping("/mission")
@Slf4j
public class MissionController {

    @GetMapping("/{userNo}")
    public ResponseEntity<String> mission(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }
//    public ResponseEntity<Map<String, Object>> mission(@RequestHeader("Authorization") String token,
//                                                       @RequestBody Map<String, Object> map,
//                                                       @PathVariable Long id) {
//        // id를 통해 userMission 조회 후 반환 (로직 추가 필요)
//        // 예: missionService.findUserMissionById(id);
//
//        // 예시 응답
//        Map<String, Object> response = new HashMap<>();
//        response.put("msg", "HelloWorld !");
//        response.put("id", id.toString());
//
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }

    @PostMapping("/{userNo}")
    public ResponseEntity<String> postMission(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

//    @PostMapping("/{userNo}")
//    public ResponseEntity<Map<String, Object>> updateMission(@PathVariable Long id, @RequestBody Map<String, Object> map) {
//        //id와 missionType을 통해 해당 type에 맞는 미션점수 설정하기
//        Map<String, Object> response = new HashMap<>();
//        response.put("msg", "HelloWorld !");
//        response.put("id", id.toString());
//
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }

}
