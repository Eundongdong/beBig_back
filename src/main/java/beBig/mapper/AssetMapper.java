package beBig.mapper;

import beBig.vo.DepositProductVo;
import beBig.dto.UserTotalAssetsDto;
import beBig.vo.TransactionVo;

import java.util.List;
import java.util.Map;

public interface AssetMapper {
    List<String> findAccountNumByUserId(long userId);
    List<TransactionVo> findTransactionsByAccountNum(String accountNum);
//    List<DepositProductVo> getRecommendedDeposits()
    Long findTotalAssetsByAccountNum(List<String> accountNumList);
}
