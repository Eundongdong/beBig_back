package beBig.service.codef;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;

import beBig.mapper.CodefTokenMapper;
import beBig.vo.CodefTokenVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CodefTokenManager {

    private static final String OAUTH_TOKEN_URL = "https://oauth.codef.io/oauth/token";
    private static final String GRANT_TYPE = "client_credentials";
    private static final String SCOPE = "read";

    @Value("${codef.client_id}") private String clientId;
    @Value("${codef.client_secret}") private String clientSecret;

    private final CodefTokenMapper tokenMapper; // MyBatis 매퍼 주입
    private String accessToken;
    private long tokenExpiryTime; // 토큰 만료 시간을 저장

    public String getAccessToken() {
        CodefTokenVo latestToken = tokenMapper.getLatestToken();

        if (latestToken != null) {
            accessToken = latestToken.getAccessToken();
            tokenExpiryTime = latestToken.getTokenExpiryTime().getTime();
        }

        if (accessToken == null || isTokenExpired()) {
            refreshToken(); // 토큰 갱신
        }

        return accessToken;
    }

    private boolean isTokenExpired() {
        return System.currentTimeMillis() >= tokenExpiryTime;
    }

    private void refreshToken() {
        HashMap<String, String> tokenMap = requestAccessToken();
        if (tokenMap != null && tokenMap.containsKey("access_token")) {
            accessToken = tokenMap.get("access_token");
            tokenExpiryTime = System.currentTimeMillis() + 3600 * 1000;

            CodefTokenVo codefToken = new CodefTokenVo();
            codefToken.setAccessToken(accessToken);
            codefToken.setTokenExpiryTime(new java.sql.Timestamp(tokenExpiryTime));

            // DB에 토큰 저장 또는 업데이트
            CodefTokenVo existingToken = tokenMapper.getLatestToken();
            if (existingToken != null) {
                // 이미 존재하는 토큰이 있으면 업데이트
                codefToken.setId(existingToken.getId());
                tokenMapper.updateToken(codefToken);
            } else {
                // 새로운 토큰이면 삽입
                tokenMapper.insertToken(codefToken);
            }
        } else {
            throw new RuntimeException("토큰 발급 실패");
        }
    }

    /**
     * 실제로 Codef API를 통해 엑세스 토큰을 발급하는 메서드
     * @return 토큰 정보를 담은 HashMap
     */
    private HashMap<String, String> requestAccessToken() {
        HttpURLConnection con = null;
        try {
            URL url = new URL(OAUTH_TOKEN_URL);
            String params = "grant_type=" + GRANT_TYPE + "&scope=" + SCOPE;
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String auth = clientId + ":" + clientSecret;
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
            con.setRequestProperty("Authorization", authHeader);
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                os.write(params.getBytes());
                os.flush();
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    StringBuilder responseStr = new StringBuilder();
                    String inputLine;
                    while ((inputLine = reader.readLine()) != null) {
                        responseStr.append(inputLine);
                    }

                    // JSON 응답을 파싱하여 토큰 정보를 가져옴
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(responseStr.toString(), new TypeReference<HashMap<String, String>>() {});
                }
            } else {
                System.err.println("Error: HTTP response code " + responseCode);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }
}
