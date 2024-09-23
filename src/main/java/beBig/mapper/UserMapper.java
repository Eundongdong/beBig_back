package beBig.mapper;

import beBig.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper {
    void insert(UserVo userVo);
    UserVo findByUserId(String userId);
    boolean isUserIdDuplicated(String userId); //아이디 중복체크
}
