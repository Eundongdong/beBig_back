package beBig.service;

import beBig.vo.AccountVo;
import beBig.vo.UserVo;

import java.util.List;

public interface HomeService {
    UserVo getUserInfo(Long userId) throws Exception;
    List<AccountVo> showMyAccount(Long userId) throws Exception;
}
