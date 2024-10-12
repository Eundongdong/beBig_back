package beBig.mapper;

import beBig.vo.*;
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

    int isUserLoginIdDuplicated(String userLoginId); //아이디 중복체크

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

    void updateUserAgeRange(@Param("userId") Long userId, @Param("ageRange") int ageRange);

    Long findUserIdByUserLoginId(@Param("userLoginId") String userLoginId);

    void clearRefreshTokenByUserId(@Param("userId") Long userId);

    int countRefreshTokenByUserId(Long userId);

    int checkDuplicateEmail(@Param("email") String email);

    int checkDuplicateNickname(@Param("nickname") String nickname);

    // 특정 유저 정보를 가져오는 메서드
    UserVo findUserByUserId(long userId);

    // 동일한 나이대의 유저 리스트를 가져오는 메서드
    List<UserVo> findUserWithSameAgeRange(int ageRange);

}
