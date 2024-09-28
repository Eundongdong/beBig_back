package beBig.service.codef;

import beBig.form.AccountForm;
import beBig.form.CodefResponseForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CodefApiRequester {

    private final CodefTokenManager tokenManager;

    // 계정 등록 요청 메서드
    public boolean registerConnectedId(String requestBody) throws Exception {
        // 토큰이 유효한지 확인하고,
        // https://development.codef.io/v1/account/create 여기로 등록 요청
        return true;
    }

    public boolean addConnectedId(String requestBody) throws Exception {
        // 토큰이 유효한지 확인하고,
        // https://development.codef.io/v1/account/add 여기로 추가 요청
        return true;
    }

    // ---------------------은행과 연결 완료---------------------------

    public List<CodefResponseForm> getAccountInfo(AccountForm accountForm, String connectedId) throws Exception {
        // AccountForm에서 은행코드 불러와서 connectedId와 함께 https://development.codef.io/v1/kr/bank/p/account/account-list 여기로 요청,
        // CodefApiResonseForm으로 받아와
        return null;
    }

}