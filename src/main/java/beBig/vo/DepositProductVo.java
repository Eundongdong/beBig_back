package beBig.vo;

public class DepositProductVo {
    private String depositProductCode;
    private int depositProductBankId;
    private String depositProductName;
    private int depositProductTerm;
    private double depositProductRate;
    private double depositProductMaxRate;

    private BankVo bank; // FK - 은행 정보
}
