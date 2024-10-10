package beBig.service;

import beBig.dto.*;
import beBig.dto.response.FinInfoResponseDto;
import beBig.vo.FinTestVo;
import beBig.vo.UserVo;

import java.util.List;

public interface HomeService {
    UserVo getUserInfo(Long userId) throws Exception;
  
       List<AccountResponseDto> showMyAccount(Long userId) throws Exception;

    List<CodefAccountDto> addAccount(Long userId, AccountRequestDto accountRequestDto) throws Exception;

    boolean saveTransactions(Long userId, String accountNum, int days) throws Exception;

    AccountTransactionDto getTransactionList(Long userId, String accountNum, int page, int pageSize);

    List<FinTestVo> findMission();

    FinInfoResponseDto findFinTypeByUserId(Long userId);

    void saveUserFinType(Long userId, int userFinType);

    void updateTransactions() throws Exception;
}
