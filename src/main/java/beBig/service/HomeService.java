package beBig.service;

import beBig.dto.*;
import beBig.vo.AccountVo;
import beBig.vo.UserVo;

import java.util.List;

public interface HomeService {
    UserVo getUserInfo(Long userId) throws Exception;
    List<CodefAccountDto> getUserAccount(Long userId, AccountRequestDto accountRequestDto) throws Exception;
    boolean addAccountToDB(Long userId, List<CodefAccountDto> codefAccountDtoList);

    void saveTransactions(Long userId, CodefTransactionRequestDto requestDto) throws Exception;

    List<AccountResponseDto> showMyAccount(Long userId) throws Exception;

    AccountTransactionDto getTransactionList(Long userId, String accountNum);
}
