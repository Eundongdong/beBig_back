package beBig.service;

import beBig.mapper.DepositApiMapper;
import beBig.vo.DepositProductVo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositApiServiceImp implements DepositApiService {

    private final DepositApiMapper depositApiMapper;

    // 트랜잭션 처리
    @Transactional
    public void fetchAndSaveDepositData() throws IOException {
        String apiUrl = "http://finlife.fss.or.kr/finlifeapi/depositProductsSearch.json?auth=626547e6ff50311419428c3db608f9ff&topFinGrpNo=020000&pageNo=1";  // 실제 API URL로 변경
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setInstanceFollowRedirects(false);  // 리다이렉트 자동으로 따르기

        // 응답 상태 코드 확인
        int responseCode = conn.getResponseCode();

        // 307 리다이렉트 처리
        if (responseCode == 307 || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
            String newUrl = conn.getHeaderField("Location");

            // 새로운 URL로 다시 요청 보내기
            url = new URL(newUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            responseCode = conn.getResponseCode();  // 새로운 응답 상태 코드 갱신
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            log.error("HTTP 요청 실패: 응답 코드 = {}", responseCode);
            throw new RuntimeException("HTTP 요청 실패: " + responseCode);
        }

        // InputStream을 통해 JSON 데이터 읽기
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();
        conn.disconnect();

        // 응답 JSON 데이터 로그 출력
        log.info("API 응답 데이터: {}", response.toString());

        // JSON 데이터를 파싱하여 VO 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(response.toString());
        JsonNode depositList = root.path("result").path("baseList");
        JsonNode optionList = root.path("result").path("optionList");

        if (depositList.isMissingNode() || !depositList.isArray()) {
            log.error("depositList 노드가 비어있거나 잘못된 형식입니다.");
            throw new RuntimeException("depositList 노드가 비어있거나 잘못된 형식입니다.");
        }

        if (optionList.isMissingNode() || !optionList.isArray()) {
            log.error("optionList 노드가 비어있거나 잘못된 형식입니다.");
            throw new RuntimeException("optionList 노드가 비어있거나 잘못된 형식입니다.");
        }

        // 각 depositNode에서 optionList 추출
        for (JsonNode depositNode : depositList) {
            String depositProductCode = depositNode.path("fin_prdt_cd").asText();
            // fin_co_no를 문자열로 추출
            String finCoNo = depositNode.path("fin_co_no").asText();
            // 매핑 로직을 통해 bank_id 결정
            Integer depositProductBankId = mapFinCoNoToBankId(finCoNo);

            if(depositProductBankId == null) {
                log.warn("Unknown fin_co_no: {}", finCoNo);
                continue;
            }

            String depositProductName = depositNode.path("fin_prdt_nm").asText();
//            log.info("depositProductCode = {}, depositProductBankId = {}, depositProductName = {}",
//                    depositProductCode, depositProductBankId, depositProductName);

            // optionList의 데이터를 처리
            for (JsonNode optionNode : optionList) {
                // optionList의 상품 코드와 baseList의 상품 코드를 매칭
                String optionProductCode = optionNode.path("fin_prdt_cd").asText();

                if (depositProductCode.equals(optionProductCode)) {
                    int depositProductTerm = optionNode.path("save_trm").asInt();
                    double depositProductRate = optionNode.path("intr_rate").asDouble();
                    double depositProductMaxRate = optionNode.path("intr_rate2").asDouble();

                    // 가져온 데이터를 로그로 출력
//                    log.info("depositProductTerm = {}, depositProductRate = {}, depositProductMaxRate = {}",
//                            depositProductTerm, depositProductRate, depositProductMaxRate);

                    // DepositProductVo에 데이터 설정
                    DepositProductVo deposit = new DepositProductVo();
                    deposit.setDepositProductCode(depositProductCode);
                    deposit.setDepositProductBankId(depositProductBankId);
                    deposit.setDepositProductName(depositProductName);
                    deposit.setDepositProductTerm(depositProductTerm);
                    deposit.setDepositProductRate(depositProductRate);
                    deposit.setDepositProductMaxRate(depositProductMaxRate);

                    log.info("Deposit Product Data: {}", deposit);

                    // DB 저장 부분
                    depositApiMapper.insert(deposit);
                }
            }
        }
    }
    private Integer mapFinCoNoToBankId(String finCoNo) {
        switch (finCoNo) {
            case "0010927": // 국민은행
                return 1;
            case "0011625": // 신한은행
                return 2;
            case "0010026": // 기업은행
                return 3;
            case "0010001": // 우리은행
                return 4;
            case "0013175": // 농협은행
                return 5;
            case "0013909": // 하나은행
                return 6;
            default:
                return null;
        }
    }

}


