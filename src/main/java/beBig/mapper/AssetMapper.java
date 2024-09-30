package beBig.mapper;

import beBig.dto.UserTotalAssetsDto;
import beBig.vo.TransactionVo;

import java.util.List;

public interface AssetMapper {
    List<String> findAccountNumByUserId(long userId);
    List<TransactionVo> findTransactionsByAccountNum(String accountNum);
    List<UserTotalAssetsDto> findTotalAssetsByAge(long userId);
}
