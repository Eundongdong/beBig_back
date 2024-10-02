package beBig.mapper;

import beBig.dto.SavingsProductDto;
import beBig.vo.SavingsProductVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface SavingsProductMapper {
    void insertSavingsProduct(SavingsProductDto savingsProductDto);
    List<SavingsProductVo> getTop2RecommendedSavingsProduct(Map<String, Object> params);
    List<SavingsProductVo> showSavingsProductsExcludingPrimaryBanks(Map<String, Object> params);
}
