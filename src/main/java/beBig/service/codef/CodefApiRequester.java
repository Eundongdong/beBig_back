package beBig.service.codef;

import beBig.form.AccountForm;
import beBig.form.CodefResponseForm;
import beBig.mapper.AccountMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
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
public class CodefApiRequester {

    @Value("${codef.RSA_public_key}")
    String PUBLIC_KEY;
    private final CodefTokenManager codefTokenManager;
    private final SqlSessionTemplate sqlSessionTemplate;

    // ConnectedId 계정 등록 요청 메서드
    public String registerConnectedId(AccountForm accountForm) throws Exception {
        // 토큰이 유효한지 확인하고,
        String accessToken = codefTokenManager.getAccessToken();
        String requestUrl = "https://api.codef.io/v1/account/create";

        // 계정 등록 요청 바디 생성 (accountForm 활용)
        String requestBody = buildRequestBody(accountForm);

        // API 요청 보내기
        String response = sendPostRequest(requestUrl, accessToken, requestBody);
        System.out.println(response);
        return extractConnectedIdFromResponse(response);
    }

    // ConnectedId 계정 추가 요청 메서드
    public String addConnectedId(String connectedId, AccountForm accountForm) throws Exception {
        // 토큰이 유효한지 확인하고,
        String accessToken = codefTokenManager.getAccessToken();
        String requestUrl = "https://development.codef.io/v1/account/add";

        String requestBody = buildRequestBody(accountForm, connectedId);

        sendPostRequest(requestUrl, accessToken, requestBody);
        return connectedId;
    }

    // 요청 바디 생성
    private String buildRequestBody(AccountForm accountForm) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode accountInfo = buildAccountInfo(accountForm);

        ArrayNode accountList = mapper.createArrayNode();
        accountList.add(accountInfo);

        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.set("accountList", accountList);

        return requestBody.toString();
    }

    // 요청 바디 생성(Overload)
    private String buildRequestBody(AccountForm accountForm, String connectedId) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode accountInfo = buildAccountInfo(accountForm);

        ArrayNode accountList = mapper.createArrayNode();
        accountList.add(accountInfo);

        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.set("accountList", accountList);
        requestBody.put("connectedId", connectedId);  // connectedId 추가

        return requestBody.toString();
    }

    // accountInfo 객체 생성
    private ObjectNode buildAccountInfo(AccountForm accountForm) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode accountInfo = mapper.createObjectNode();

        accountInfo.put("countryCode", "KR");
        accountInfo.put("businessType", "BK");
        accountInfo.put("clientType", "P");
        accountInfo.put("organization", accountForm.getBank());
        accountInfo.put("loginType", "1");
        accountInfo.put("id", accountForm.getUserBankId());

        // 암호화된 비밀번호 추가
        String encryptedPassword = RSAUtil.encryptRSA(accountForm.getBankPassword(), PUBLIC_KEY);
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
    public List<CodefResponseForm> getAccountInfo(AccountForm accountForm, String connectedId) throws Exception {
        String requestUrl = "https://development.codef.io/v1/kr/bank/p/account/account-list";
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("organization", accountForm.getBank());
        requestBody.put("connectedId", connectedId);

        String response = sendPostRequest(requestUrl, codefTokenManager.getAccessToken(), requestBody.toString());

        List<CodefResponseForm> accountList = new ArrayList<>();
        JsonNode jsonResponse = mapper.readTree(response);

        // 응답에서 따로 메시지를 추출
        String message = jsonResponse.path("result").path("message").asText();

        JsonNode depositTrustList = jsonResponse.path("data").path("resDepositTrust");
        if (depositTrustList.isArray() && depositTrustList.size() > 0) {
            for (JsonNode accountInfo : depositTrustList) {
                CodefResponseForm codefResponseForm = new CodefResponseForm();
                codefResponseForm.setResAccount(accountInfo.get("resAccount").asText());
                codefResponseForm.setResAccountBalance(accountInfo.get("resAccountBalance").asText());
                codefResponseForm.setResAccountDeposit(accountInfo.get("resAccountDeposit").asText());
                codefResponseForm.setResAccountName(accountInfo.get("resAccountName").asText());
                codefResponseForm.setMessage(message);

                AccountMapper accountMapper = sqlSessionTemplate.getMapper(AccountMapper.class);
                codefResponseForm.setResAccountBank(accountMapper.getBankNameByCode(accountForm.getBank()));

                accountList.add(codefResponseForm);
            }
        }
        return accountList;
    }
}
