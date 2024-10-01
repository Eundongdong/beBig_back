package beBig.service;

import beBig.dto.response.SpendingPatternsResponseDto;
import beBig.vo.UserVo;

import java.util.Map;

public interface AssetService {
    public SpendingPatternsResponseDto showSpendingPatterns(long userId,int year);

    public Map<String, Object> showProductRecommendations(long userId);
}
