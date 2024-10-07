package beBig.service;

import beBig.dto.UserDto;
import beBig.dto.response.FinInfoResponseDto;
import beBig.vo.FinTestVo;
import beBig.vo.FinTypeVo;
import beBig.vo.UserVo;
import beBig.vo.UtilVo;

import java.util.List;

public interface UserService {
    void registerUser(UserDto userDto) throws Exception;

    boolean updatePasswordByEmail(String userLoginId, String name, String email);

    boolean isUserLoginIdDuplicated(String userId);

    String findUserLoginIdByNameAndEmail(String name, String email);

    boolean findByEmailAndLoginType(String email, String userLoginType);

    List<UtilVo> getUtilTerms(); // 약관조회

    Long findUserIdByKakaoId(String kakaoId);

    void removeRefreshToken(String refreshToken);

    Long findUserIdByUserLoginId(String userLoginId);

    void deleteRefreshTokenBeforeLogin(Long userId);

    boolean checkIfRefreshTokenExistsByUserId(Long userId);

}
