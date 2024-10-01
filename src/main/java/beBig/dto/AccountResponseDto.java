package beBig.dto;

import lombok.Data;

@Data
public class AccountResponseDto {
    private String accountNum;       // 계좌 번호
    private String accountName;      // 계좌 이름
    private String bankName;         // 은행 이름
    private int transactionBalance;   // 가장 최근 거래 잔액
}
