package beBig.service;

import beBig.dto.AccountDto;
import beBig.dto.AccountTransactionDto;
import beBig.dto.CodefAccountDto;
import beBig.dto.CodefTransactionRequestDto;
import beBig.vo.AccountVo;
import beBig.vo.UserVo;

import java.util.List;

public interface HomeService {
    UserVo getUserInfo(Long userId) throws Exception;
    List<CodefAccountDto> getUserAccount(Long userId, AccountDto accountDto) throws Exception;
    boolean addAccountToDB(Long userId, List<CodefAccountDto> codefAccountDtoList);

    void saveTransactions(Long userId, CodefTransactionRequestDto requestDto) throws Exception;

    List<AccountVo> showMyAccount(Long userId) throws Exception;

    AccountTransactionDto getTransactionList(Long userId, String accountNum);
}
