package beBig.mapper;

import beBig.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;


@Mapper
public interface UserMapper {
    void insert(UserVo userVo);

    UserVo findByUserId(String userId);

    boolean isUserIdDuplicated(String userId); //아이디 중복체크

    String findUserIdByNameAndEmail(Map<String, Object> params); // 아이디 찾기

    int updatePasswordByUserIdAndEmail(Map<String, Object> params);

    boolean findByUserIdAndLoginType(@Param("userId") String userId, @Param("loginType") String loginType);
}
