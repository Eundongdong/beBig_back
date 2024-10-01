package beBig.controller;

import beBig.dto.UserTotalAssetsDto;
import beBig.dto.response.AgeComparisonResponseDto;
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

    @GetMapping("/{userNo}/product-recommendations")
    public ResponseEntity<String> productRecommendations(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @GetMapping("/{userId}/age-comparison")//@RequestHeader("Authorization") String token
    public ResponseEntity<UserTotalAssetsDto> ageComparison(@PathVariable Long userId) {
        //        long userId = jwtUtil.extractUserIdFromToken(token);
        if(userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        UserTotalAssetsDto userTotalAssetsDto = assetService.showAgeComparison(userId);
        return ResponseEntity.status(HttpStatus.OK).body(userTotalAssetsDto);
    }
}
