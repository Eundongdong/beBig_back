package beBig.vo;

import lombok.Data;

@Data
public class TransactionVo {
    private int transactionId;
    private String transactionAccountNum;
    private String transactionVendor;
    private int transactionBalance;
    private int transactionAmount;
    private String transactionDate; // 추후 유틸 클래스를 통해 Date로 변환예정
    private String transactionType;
}
