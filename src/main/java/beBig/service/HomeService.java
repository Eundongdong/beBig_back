package beBig.service;

import beBig.vo.UserVo;

public interface HomeService {
    UserVo getUserInfo(Long userId) throws Exception;
}
