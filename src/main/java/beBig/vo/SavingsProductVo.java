package beBig.vo;

public class SavingsProductVo {
    private String savingsProductCode;
    private int savingsProductBankId;
    private String savingsProductName;
    private int savingsProductTerm;
    private double savingsProductRate;
    private double savingsProductMaxRate;

    private BankVo bank; // FK - 은행 정보
}
