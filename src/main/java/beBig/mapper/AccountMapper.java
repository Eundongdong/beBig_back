package beBig.mapper;

import beBig.dto.AccountResponseDto;
import beBig.vo.AccountVo;
import beBig.vo.BankVo;
import beBig.vo.TransactionVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface AccountMapper {

    List<AccountVo> findAccountById(String connectedId);
    int findPrimaryBankId(long userId);
    List<Integer> findAllOtherBanksExceptPrimary(int primaryBankId);

    List<AccountVo> findAccountById(Long userId);
    
    BankVo getBankByCode(String bankCode);

    BankVo getBankById(int id);

    void insertTransaction(TransactionVo transactionVo);

    List<TransactionVo> getTransactionsByAccountNum(
            @Param("accountNum")String accountNum,
            @Param("limit") int limit,
            @Param("offset") int offset);

    long getTransactionCountByAccountNum(String accountNum);

    AccountVo findAccountByAccountNum(String accountNum);

    List<TransactionVo> findTransactionsByAccountNumAndDateRange(
            @Param("accountNum") String accountNum,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    List<AccountResponseDto> findAccountDetailsByUserId(Long userId);

    String findBankCodeByBankId(int bankId);

    int findBankIdByAccountNum(String accountNum);

    List<AccountVo> findAllAccounts();

    void insertTransactionBatch(List<TransactionVo> transactions);

    void insertAccount(List<AccountVo> accountsToInsert);
}
