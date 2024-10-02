package beBig.controller;

import beBig.service.SavingsProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/savings")
public class SavingsApiController {

    @Autowired
    private SavingsProductService savingsProductService;

    @PostMapping("/save")
    public String saveSavingsProducts(@RequestBody String jsonData) {
        try {
            savingsProductService.saveSavingsProductData(jsonData);
            log.info("적금 상품이 성공적으로 저장되었습니다.");
            return "적금 상품이 성공적으로 저장되었습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            log.error("적금 상품 저장에 실패하였습니다.", e);
            return "적금 상품 저장에 실패하였습니다.";
        }
    }
}