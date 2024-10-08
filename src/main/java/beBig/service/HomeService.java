package beBig.service;

import beBig.dto.*;
import beBig.dto.response.FinInfoResponseDto;
import beBig.vo.AccountVo;
import beBig.vo.FinTestVo;
import beBig.vo.UserVo;

import java.util.List;

public interface HomeService {
    UserVo getUserInfo(Long userId) throws Exception;
  
    List<CodefAccountDto> getUserAccount(Long userId, AccountRequestDto accountRequestDto) throws Exception;
  
    boolean addAccountToDB(Long userId, List<CodefAccountDto> codefAccountDtoList);

    List<AccountResponseDto> showMyAccount(Long userId) throws Exception;

    AccountTransactionDto getTransactionList(Long userId, String accountNum);

    boolean saveTransactions(Long userId, String accountNum) throws Exception;

    List<FinTestVo> findMission();

    FinInfoResponseDto findFinTypeByUserId(Long userId);

    void saveUserFinType(Long userId, int userFinType);

    void updateTransactions() throws Exception;
}
