package beBig.dto;

import beBig.vo.TransactionVo;
import lombok.Data;

import java.util.List;

@Data
public class AccountTransactionDto {
    private List<TransactionVo> transactions; // 거래 내역 리스트
    private String accountName; // 계좌 이름
    private String bankName;    // 은행 이름
}