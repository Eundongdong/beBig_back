package beBig.mapper;

import beBig.vo.AccountVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AccountMapper {
    List<AccountVo> findAccountById(String connectedId);

    String getBankNameByCode(String bankCode);
}
