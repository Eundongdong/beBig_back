package beBig.dto;

import beBig.vo.BankVo;
import lombok.Data;

@Data
public class CodefResponseDto {
    private String resAccountBalance;        // 현재잔액
    private String resAccount;               // 계좌번호
    private String resAccountDeposit;        // 예금구분
    private String resAccountName;           // 계좌명(종류)
    private BankVo bankVo;
    private String message;                   // 응답 메시지
}