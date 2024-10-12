package beBig.vo;

import lombok.Data;

@Data
public class AssetUserBalanceVo {
    private long userId;
    private int userAgeRange;
    private long totalBalance;
}
