package beBig.mapper;

import beBig.vo.DepositProductVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface DepositProductMapper {
    void insert(DepositProductVo depositProductVo);
    List<DepositProductVo> getTop2RecommendedDepositProducts(Map<String, Object> params);
    List<DepositProductVo> showDepositProductsExcludingPrimaryBanks(Map<String, Object> params);
}
