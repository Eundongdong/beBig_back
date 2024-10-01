package beBig.mapper;

import beBig.vo.AccountVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface AccountMapper {
    List<AccountVo> findAccountById(String connectedId);
    int findPrimaryBankId(long userId);
    List<Integer> findOtherBankIds(Map<String, Object> params);
}
