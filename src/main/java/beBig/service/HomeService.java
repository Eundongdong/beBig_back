package beBig.service;

import beBig.dto.AccountDto;
import beBig.dto.CodefResponseDto;
import beBig.vo.AccountVo;
import beBig.vo.UserVo;

import java.util.List;

public interface HomeService {
    UserVo getUserInfo(Long userId) throws Exception;
    List<CodefResponseDto> getUserAccount(Long userId, AccountDto accountDto) throws Exception;
    boolean addAccountToDB(Long userId, List<CodefResponseDto> codefResponseDtoList);
    List<AccountVo> showMyAccount(Long userId) throws Exception;
}
