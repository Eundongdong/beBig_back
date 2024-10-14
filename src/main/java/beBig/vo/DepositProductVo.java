package beBig.vo;

import lombok.Data;

@Data
public class DepositProductVo {
    private String depositProductCode;
    private int bankId;
    private String depositProductName;
    private int depositProductTerm;
    private double depositProductRate;
    private double depositProductMaxRate;
    private String depositProductUrl;

    private BankVo bank; // FK - 은행 정보
}
