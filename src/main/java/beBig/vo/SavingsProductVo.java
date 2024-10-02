package beBig.vo;

import lombok.Data;

@Data
public class SavingsProductVo {
    private String savingsProductCode;
    private int bankId;
    private String savingsProductName;
    private String savingsProductType;
    private int savingsProductTerm;
    private double savingsProductRate;
    private double savingsProductMaxRate;

    private BankVo bank; // FK - 은행 정보
}
