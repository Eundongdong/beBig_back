package beBig.service;

import beBig.dto.UserTotalAssetsDto;
import beBig.dto.response.SpendingPatternsResponseDto;

public interface AssetService {
    public SpendingPatternsResponseDto showSpendingPatterns(long userId,int year);
    public UserTotalAssetsDto showAgeComparison(long userId);
}
