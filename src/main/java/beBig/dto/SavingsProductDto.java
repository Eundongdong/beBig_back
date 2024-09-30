package beBig.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SavingsProductDto {
    private String savingsProductCode;
    private int bankId;  // bank_id는 int 타입
    private String savingsProductName;
    private String savingsProductType;  // "자유적립식" 또는 "정액적립식"
    private int savingsProductTerm;
    private BigDecimal savingsProductRate;
    private BigDecimal savingsProductMaxRate;
}
