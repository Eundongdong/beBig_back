package beBig.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AssetAnalysisDto {
    private long totalCashBalance;
    private long totalDepositSavingsBalance;
    private long totalBalance;
}
