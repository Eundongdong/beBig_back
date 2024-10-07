package beBig.service.codef;

import beBig.dto.AccountRequestDto;
import beBig.dto.CodefAccountDto;
import beBig.dto.CodefTransactionRequestDto;
import beBig.dto.CodefTransactionResponseDto;
import beBig.mapper.AccountMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodefApiRequester {

    private final AccountMapper accountMapper;
    @Value("${codef.RSA_public_key}")
    String PUBLIC_KEY;
    private final CodefTokenManager codefTokenManager;
    private final SqlSessionTemplate sqlSessionTemplate;

    // ConnectedId 계정 등록 요청 메서드
    public String registerConnectedId(AccountRequestDto accountRequestDto) throws Exception {
        // 토큰이 유효한지 확인하고,
        String accessToken = codefTokenManager.getAccessToken();
        String requestUrl = "https://development.codef.io/v1/account/create";

        // 계정 등록 요청 바디 생성 (accountForm 활용)
        String requestBody = buildRequestBody(accountRequestDto);

        // API 요청 보내기
        String response = sendPostRequest(requestUrl, accessToken, requestBody);
        log.info(response);
        // 에러 메시지 추출
        String errorMessage = extractErrorMessage(response);
        if (errorMessage != null) {
            throw new Exception(errorMessage);  // 에러가 있으면 예외 발생
        }

        return extractConnectedIdFromResponse(response);
    }

    // ConnectedId 계정 추가 요청 메서드
    public String addConnectedId(String connectedId, AccountRequestDto accountRequestDto) throws Exception {
        // 토큰이 유효한지 확인하고,
        String accessToken = codefTokenManager.getAccessToken();
        String requestUrl = "https://development.codef.io/v1/account/add";

        String requestBody = buildRequestBody(accountRequestDto, connectedId);

        String response = sendPostRequest(requestUrl, accessToken, requestBody);
        log.info(response);

        // 에러 메시지 추출
        String errorMessage = extractErrorMessage(response);
        if (errorMessage != null) {
            throw new Exception(errorMessage);  // 에러가 있으면 예외 발생
        }

        return connectedId;
    }

    private String extractErrorMessage(String response) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonResponse = mapper.readTree(response);

        // errorList 확인
        JsonNode errorList = jsonResponse.path("data").path("errorList");
        if (errorList.isArray() && errorList.size() > 0) {
            JsonNode errorDetail = errorList.get(0);  // 첫 번째 에러만 처리
            String errorCode = errorDetail.path("code").asText();
            String errorMessage = errorDetail.path("message").asText();

            // 특정 에러 코드에 대한 커스텀 메시지 처리
            if ("CF-04004".equals(errorCode)) {
                return "기존에 연결한 계좌가 존재합니다.";  // 커스텀 메시지
            }

            // 기본 메시지 반환
            return errorMessage;
        }

        return null;  // 에러가 없으면 null 반환
    }


    // 요청 바디 생성
    private String buildRequestBody(AccountRequestDto accountRequestDto) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode accountInfo = buildAccountInfo(accountRequestDto);

        ArrayNode accountList = mapper.createArrayNode();
        accountList.add(accountInfo);

        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.set("accountList", accountList);

        return requestBody.toString();
    }

    // 요청 바디 생성(Overload)
    private String buildRequestBody(AccountRequestDto accountRequestDto, String connectedId) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode accountInfo = buildAccountInfo(accountRequestDto);

        ArrayNode accountList = mapper.createArrayNode();
        accountList.add(accountInfo);

        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.set("accountList", accountList);
        requestBody.put("connectedId", connectedId);  // connectedId 추가

        return requestBody.toString();
    }

    // accountInfo 객체 생성
    private ObjectNode buildAccountInfo(AccountRequestDto accountRequestDto) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode accountInfo = mapper.createObjectNode();

        accountInfo.put("countryCode", "KR");
        accountInfo.put("businessType", "BK");
        accountInfo.put("clientType", "P");
        accountInfo.put("organization", accountRequestDto.getBank());
        accountInfo.put("loginType", "1");
        accountInfo.put("id", accountRequestDto.getUserBankId());

        // 암호화된 비밀번호 추가
        String encryptedPassword = RSAUtil.encryptRSA(accountRequestDto.getBankPassword(), PUBLIC_KEY);
        accountInfo.put("password", encryptedPassword);

        return accountInfo;
    }

    // API 요청 보내는 메서드
    private String sendPostRequest(String requestUrl, String accessToken, String requestBody) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // 응답을 URL 디코딩
            String decodedResponse = URLDecoder.decode(response.body(), "UTF-8");
            return decodedResponse;
        } else {
            throw new IOException("HTTP error code: " + response.statusCode());
        }
    }

    // ConnectedId를 API 요청으로부터 추출하는 메서드
    private String extractConnectedIdFromResponse(String response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = (ObjectNode) mapper.readTree(response);

        // "data" 객체 안에 있는 "connectedId" 필드 추출
        if (rootNode.has("data") && rootNode.get("data").has("connectedId")) {
            return rootNode.get("data").get("connectedId").asText();
        } else {
            throw new IOException("connectedId not found in response");
        }
    }

    // 계좌 정보 조회 메서드
    public List<CodefAccountDto> getAccountInfo(AccountRequestDto accountRequestDto, String connectedId) throws Exception {
        String requestUrl = "https://development.codef.io/v1/kr/bank/p/account/account-list";
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("organization", accountRequestDto.getBank());
        requestBody.put("connectedId", connectedId);

        String response = sendPostRequest(requestUrl, codefTokenManager.getAccessToken(), requestBody.toString());

        List<CodefAccountDto> accountList = new ArrayList<>();
        JsonNode jsonResponse = mapper.readTree(response);

        // 응답에서 따로 메시지를 추출
        String message = jsonResponse.path("result").path("message").asText();

        JsonNode depositTrustList = jsonResponse.path("data").path("resDepositTrust");
        if (depositTrustList.isArray() && depositTrustList.size() > 0) {
            for (JsonNode accountInfo : depositTrustList) {
                CodefAccountDto codefAccountDto = new CodefAccountDto();
                String resAccount = (accountInfo.get("resAccount").asText());
                if(accountMapper.findAccountByAccountNum(resAccount)!=null){
                    continue;
                } else{
                    codefAccountDto.setResAccount(resAccount);
                }
                codefAccountDto.setResAccountBalance(accountInfo.get("resAccountBalance").asText());
                codefAccountDto.setResAccountDeposit(accountInfo.get("resAccountDeposit").asText());
                codefAccountDto.setResAccountName(accountInfo.get("resAccountName").asText());
                codefAccountDto.setMessage(message);

                AccountMapper accountMapper = sqlSessionTemplate.getMapper(AccountMapper.class);
                codefAccountDto.setBankVo(accountMapper.getBankByCode(accountRequestDto.getBank()));

                accountList.add(codefAccountDto);
            }
        }
        return accountList;
    }

    public CodefTransactionResponseDto getTransactionHistory(CodefTransactionRequestDto requestDto) throws Exception {
        String accessToken = codefTokenManager.getAccessToken();
        String requestUrl = "https://development.codef.io/v1/kr/bank/p/account/transaction-list";

        // 요청 바디 생성
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // API 요청 보내기
        String response = sendPostRequest(requestUrl, accessToken, requestBody);

        // 응답 파싱
        JsonNode jsonNode = objectMapper.readTree(response);

        // 결과 코드 체크
        String resultCode = jsonNode.path("result").path("code").asText();
        if (!"CF-00000".equals(resultCode)) {
            throw new Exception("Codef API error: " + jsonNode.path("result").path("message").asText());
        }

        // CodefTransactionResponseDto 생성
        CodefTransactionResponseDto transactionResponseDto = new CodefTransactionResponseDto();
        transactionResponseDto.setAccountNum(jsonNode.path("data").path("resAccount").asText());

        // 거래 내역 리스트 존재 여부 체크
        ArrayNode transactionArray = (ArrayNode) jsonNode.path("data").path("resTrHistoryList");
        List<CodefTransactionResponseDto.HistoryItem> historyItems = new ArrayList<>();

        // 각 항목을 DTO로 변환하여 리스트에 추가
        for (JsonNode node : transactionArray) {
            CodefTransactionResponseDto.HistoryItem historyItem = objectMapper.treeToValue(node, CodefTransactionResponseDto.HistoryItem.class);
            historyItems.add(historyItem);
        }

        transactionResponseDto.setResTrHistoryList(historyItems);
        return transactionResponseDto;
    }

}