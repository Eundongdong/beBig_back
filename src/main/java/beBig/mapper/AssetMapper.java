package beBig.mapper;

import beBig.vo.AccountVo;
import beBig.vo.AssetUserBalanceVo;
import beBig.vo.DepositProductVo;
import beBig.dto.UserTotalAssetsDto;
import beBig.vo.TransactionVo;
import org.apache.ibatis.annotations.Param;

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


    // 동일한 나이대의 유저들의 총 자산을 계산하는 메서드
    List<UserTotalAssetsDto> findTotalAssetsByAgeRange(int ageRange);
    // 특정 유저의 계좌별 잔액을 조회하는 메서드
    List<AccountVo> findAccountNumByPartUserId(long userId);

    List<AssetUserBalanceVo> findUserBalance(@Param("userId") long userId);

}
