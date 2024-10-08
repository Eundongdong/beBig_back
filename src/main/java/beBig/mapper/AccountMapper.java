package beBig.mapper;

import beBig.dto.AccountResponseDto;
import beBig.vo.AccountVo;
import beBig.vo.BankVo;
import beBig.vo.TransactionVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface AccountMapper {

    List<AccountVo> findAccountById(String connectedId);
    int findPrimaryBankId(long userId);
    List<Integer> findAllOtherBanksExceptPrimary(int primaryBankId);

    List<AccountVo> findAccountById(Long userId);

    void insertAccount(AccountVo accountVo);

    BankVo getBankByCode(String bankCode);

    BankVo getBankById(int id);

    void insertTransaction(TransactionVo transactionVo);

    List<TransactionVo> getTransactionsByAccountNum(String accountNum);

    AccountVo findAccountByAccountNum(String accountNum);

    TransactionVo findTransactionByDateAndAmount(
            @Param("accountNum") String accountNum,
            @Param("transactionDate") Date transactionDate,
            @Param("transactionAmount") Integer transactionAmount); // 중복 거래 조회

    List<AccountResponseDto> findAccountDetailsByUserId(Long userId);

    String findBankCodeByBankId(int bankId);

    int findBankIdByAccountNum(String accountNum);

    List<AccountVo> findAllAccounts();

}
