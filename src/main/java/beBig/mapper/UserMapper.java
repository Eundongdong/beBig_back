package beBig.mapper;

import beBig.vo.UserVo;
import beBig.vo.UtilVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


@Mapper
public interface UserMapper {
    void insert(UserVo userVo);

    UserVo findByUserId(Long userId);

    UserVo findByUserLoginId(String userLoginId);

    boolean isUserLoginIdDuplicated(String userLoginId); //아이디 중복체크

    String findUserLoginIdByNameAndEmail(Map<String, Object> params); // 아이디 찾기

    int updatePasswordByUserLoginIdAndEmail(Map<String, Object> params);

    boolean findByEmailAndLoginType(@Param("email") String email, @Param("loginType") String loginType);

    List<UtilVo> getUtilTerms(); // 약관 조회 메서드
}
