package beBig.vo;

import lombok.Data;

import java.util.List;

@Data
public class AccountVo {
    private String accountNum;
    private int accountBankId;
    private String accountName;
    private String accountConnectedId;

    private List<TransactionVo> transactions;  // 거래 목록
}
