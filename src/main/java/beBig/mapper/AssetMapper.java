package beBig.mapper;

import beBig.vo.DepositProductVo;
import beBig.dto.UserTotalAssetsDto;
import beBig.vo.TransactionVo;

import java.util.List;
import java.util.Map;

public interface AssetMapper {
    List<String> findAccountNumByUserId(long userId);
    String findAccountTypeByAccountNum(String accountNum);
    Long showLatestCashBalance(Map<String, Object> params);
    Long showLatestDepositBalance(Map<String, Object> params);
    Long showLatestEtcBalance(Map<String, Object> params);
    List<TransactionVo> findTransactionsByAccountNum(String accountNum);
//    List<DepositProductVo> getRecommendedDeposits()
    Long findTotalAssetsByAccountNum(List<String> accountNumList);
}
