package beBig.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@Controller
@RequestMapping("/asset")
@Slf4j
public class AssetController {
    @GetMapping("/{userNo}/analysis")
    public ResponseEntity<String> assetAnalysis(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @GetMapping("/{userNo}/spending-patterns")
    public ResponseEntity<String> spendingPatterns(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @GetMapping("/{userNo}/product-recommendations")
    public ResponseEntity<String> productRecommendations(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }

    @GetMapping("/{userNo}/age-comparison")
    public ResponseEntity<String> ageComparison(@PathVariable Long userNo) {
        return ResponseEntity.status(HttpStatus.OK).body("Hello World!");
    }
}
