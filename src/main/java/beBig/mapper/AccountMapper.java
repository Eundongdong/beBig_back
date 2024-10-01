package beBig.mapper;

import beBig.dto.AccountResponseDto;
import beBig.vo.AccountVo;
import beBig.vo.BankVo;
import beBig.vo.TransactionVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AccountMapper {
    List<AccountVo> findAccountById(Long userId);

    void insertAccount(AccountVo accountVo);

    BankVo getBankByCode(String bankCode);

    BankVo getBankById(int id);

    void insertTransaction(TransactionVo transactionVo);

    List<TransactionVo> getTransactionsByAccountNum(String accountNum);

    AccountVo findAccountByAccountNum(String accountNum);

    List<AccountResponseDto> findAccountDetailsByUserId(Long userId);
}
