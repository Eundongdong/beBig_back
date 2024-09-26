package beBig.service;

import beBig.form.UserForm;
import beBig.vo.UserVo;
import beBig.vo.UtilVo;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UserService {
    void registerUser(UserForm userForm) throws Exception;

    boolean updatePasswordByEmail(String userId, String name, String email);

    boolean isUserLoginIdDuplicated(String userId);

    String findUserLoginIdByNameAndEmail(String name, String email);

    boolean findByEmailAndLoginType(String email, String userLoginType);

    List<UtilVo> getUtilTerms(); // 약관조회
}
