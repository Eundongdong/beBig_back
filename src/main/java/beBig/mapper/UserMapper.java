package beBig.mapper;

import beBig.vo.FinTestVo;
import beBig.vo.FinTypeVo;
import beBig.vo.UserVo;
import beBig.vo.UtilVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
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

    void updateUserConnectedId(@Param("userId") Long userId, @Param("connectedId") String connectedId);

    Long getUserIdByKaKaoId(String userLoginId);

    void saveRefreshTokenUser(@Param("userId") long userId, @Param("refreshToken") String refreshToken);

    Long getUserIdByRefreshToken(@Param("refreshToken") String refreshToken);

    LocalDateTime getExpiredTimeByRefreshToken(@Param("refreshToken") String refreshToken);

    void clearRefreshTokenRT(@Param("refreshToken") String refreshToken);

    void saveRefreshTokenRT(@Param("userId") Long userId, @Param("refreshToken") String refreshToken, @Param("expiryDate") Date expiryDate);

    FinTypeVo findFinTypeByUserId(long userId);

    List<UserVo> findBySameAgeRange(long userId);
    void clearRefreshTokenUser(@Param("refreshToken") String refreshToken);

    List<UserVo> getAllUsers();

    void updateUserAgeRange(@Param("userId")Long userId, @Param("ageRange")int ageRange);
}
