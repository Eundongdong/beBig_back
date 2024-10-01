package beBig.service;

import beBig.dto.response.FinInfoResponseDto;
import beBig.vo.AccountVo;
import beBig.vo.FinTestVo;
import beBig.vo.UserVo;

import java.util.List;

public interface HomeService {
    UserVo getUserInfo(Long userId) throws Exception;

    List<AccountVo> showMyAccount(Long userId) throws Exception;

    List<FinTestVo> findMission();

    FinInfoResponseDto findFinTypeByUserId(Long userId);

    void saveUserFinType(Long userId, int userFinType);
}
