package beBig.controller;

import beBig.service.DepositApiService;
import beBig.service.DepositApiServiceImp;
import beBig.vo.DepositProductVo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@CrossOrigin("*")
@Controller
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class DepositApiController {
    private DepositApiService depositApiService;

    @Autowired
    public DepositApiController(DepositApiService depositApiService) {
        this.depositApiService = depositApiService;
    }

    @PostMapping("/fetch")
    public ResponseEntity<String> fetchDepositData() {
        try {
            // 서비스 레이어에서 외부 API 호출
            depositApiService.fetchAndSaveDepositData();
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail: " + e.getMessage());
        }
    }

}
