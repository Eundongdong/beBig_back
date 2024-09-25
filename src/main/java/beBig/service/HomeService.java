package beBig.service;

import beBig.vo.UserVo;

public interface HomeService {
    UserVo getUserInfo(String userId) throws Exception;
}
