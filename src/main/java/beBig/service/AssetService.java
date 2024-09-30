package beBig.service;

import beBig.dto.response.AgeComparisonResponseDto;
import beBig.dto.response.SpendingPatternsResponseDto;

public interface AssetService {
    public SpendingPatternsResponseDto showSpendingPatterns(long userId,int year);
    public AgeComparisonResponseDto showAgeComparison(long userId);
}
