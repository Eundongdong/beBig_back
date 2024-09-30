package beBig.mapper;

import beBig.vo.AccountVo;
import beBig.vo.BankVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AccountMapper {
    List<AccountVo> findAccountById(Long userId);

    void insertAccount(AccountVo accountVo);

    BankVo getBankByCode(String bankCode);
}
