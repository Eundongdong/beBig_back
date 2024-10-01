package beBig.controller;

import beBig.dto.response.SpendingPatternsResponseDto;
import beBig.service.AssetService;
import beBig.service.jwt.JwtUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin("*")
@Controller
@RequestMapping("/asset")
@Slf4j
public class AssetController {
    private JwtUtil jwtUtil;
    private AssetService assetService;

    @Autowired
    public AssetController(JwtUtil jwtUtil, AssetService assetService) {
        this.jwtUtil = jwtUtil;
        this.assetService = assetService;
    }



    @GetMapping("/{userNo}/analysis")
    public ResponseEntity<String> assetAnalysis(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @ApiOperation(value = "소비 패턴 분석")
    @GetMapping("/{userId}/spending-patterns") //@RequestHeader("Authorization") String token
    public ResponseEntity<SpendingPatternsResponseDto> spendingPatterns( @PathVariable Long userId, int year) {
//        jwtUtil.extractUserIdFromToken(token);
        SpendingPatternsResponseDto spendingPatternsResponseDto = assetService.showSpendingPatterns(userId, year);
        return ResponseEntity.status(HttpStatus.OK).body(spendingPatternsResponseDto);
    }

    @GetMapping("/product-recommendations")
    public ResponseEntity<Map<String, Object>> productRecommendations(@RequestHeader("Authorization") String token) {
        long userId = jwtUtil.extractUserIdFromToken(token);

        // 예금 및 적금 추천 정보를 가져옴
        Map<String, Object> recommendations = assetService.showProductRecommendations(userId);

        if(recommendations.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(recommendations);
    }

    @GetMapping("/{userNo}/age-comparison")
    public ResponseEntity<String> ageComparison(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }
}
