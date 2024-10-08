package beBig.vo;

import lombok.Data;

import java.util.Date;

@Data
public class TransactionVo {
    private int transactionId;
    private String accountNum;
    private String transactionVendor;
    private int transactionBalance;
    private int transactionAmount;
    private Date transactionDate;
    private String transactionType;
}
