package beBig.service;

import beBig.form.UserForm;
import org.springframework.stereotype.Service;

public interface UserService {
    void registerUser(UserForm userForm) throws Exception;
    boolean updatePasswordByEmail(String userId, String name, String email);
    boolean isUserIdDuplicated(String userId);
    String findUserIdByNameAndEmail(String name, String email);
}
