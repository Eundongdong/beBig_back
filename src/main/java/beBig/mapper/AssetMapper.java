package beBig.mapper;

import beBig.vo.TransactionVo;

import java.util.List;

public interface AssetMapper {
    List<String> findAccountNumByUserId(long userId);
    List<TransactionVo> findTransactionsByAccountNum(String accountNum);
}
