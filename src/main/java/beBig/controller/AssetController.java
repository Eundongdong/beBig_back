package beBig.controller;

import beBig.dto.UserTotalAssetsDto;
import beBig.dto.response.SpendingPatternsResponseDto;
import beBig.service.AssetService;
import beBig.service.jwt.JwtUtil;
import io.jsonwebtoken.JwtException;
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
    @GetMapping("/spending-patterns")
    public ResponseEntity<SpendingPatternsResponseDto> spendingPatterns(@RequestHeader("Authorization") String token, int year) {
        try {
            long userId = jwtUtil.extractUserIdFromToken(token);
            log.info("userId = {}", userId);
            SpendingPatternsResponseDto spendingPatternsResponseDto = assetService.showSpendingPatterns(userId, year);
            return ResponseEntity.status(HttpStatus.OK).body(spendingPatternsResponseDto);
        } catch (JwtException e) {
            // JWT 토큰이 유효하지 않거나 만료된 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

        } catch (Exception e) {
            // 기타 예외 처리 (로깅 후 500 에러 반환)
            log.error("Unexpected error occurred: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
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

    @ApiOperation(value = "같은 나잇대 중 총자산 순위 조회")
    @GetMapping("/age-comparison")
    public ResponseEntity<UserTotalAssetsDto> ageComparison(@RequestHeader("Authorization") String token) {
        try{
            // JWT 토큰에서 사용자 ID 추출
            long userId = jwtUtil.extractUserIdFromToken(token);

            UserTotalAssetsDto userTotalAssetsDto = assetService.showAgeComparison(userId);
            return ResponseEntity.status(HttpStatus.OK).body(userTotalAssetsDto);

        } catch (JwtException e) {
            // JWT 토큰이 유효하지 않거나 만료된 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

        } catch (Exception e) {
            // 기타 예외 처리 (로깅 후 500 에러 반환)
            log.error("Unexpected error occurred: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
