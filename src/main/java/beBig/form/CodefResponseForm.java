package beBig.form;

import beBig.vo.AccountVo;
import lombok.Data;

import java.util.List;

@Data
public class CodefResponseForm {
    private String resDepositTrust;         // 예금/신탁
    private String resOverdraftAcctYN;      // 마이너스 통장 여부
    private String resAccountDisplay;        // 계좌번호_표시용
    private String resLastTranDate;         // 최종거래일
    private String resAccountLifetime;       // 평생계좌번호
    private String resAccountEndDate;       // 만기일
    private String resAccountStartDate;     // 신규일
    private String resAccountNickName;      // 계좌별칭
    private String resAccountCurrency;       // 통화코드
    private String resAccountBalance;        // 현재잔액
    private String resAccount;               // 계좌번호
    private String resAccountDeposit;        // 예금구분
    private String resAccountName;           // 계좌명(종류)
}