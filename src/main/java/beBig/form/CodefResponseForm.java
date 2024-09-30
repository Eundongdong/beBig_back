package beBig.form;

import beBig.vo.AccountVo;
import beBig.vo.BankVo;
import lombok.Data;

import java.util.List;

@Data
public class CodefResponseForm {
    private String resAccountBalance;        // 현재잔액
    private String resAccount;               // 계좌번호
    private String resAccountDeposit;        // 예금구분
    private String resAccountName;           // 계좌명(종류)
    private BankVo bankVo;
    private String message;                   // 응답 메시지
}