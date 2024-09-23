package beBig.form;

import lombok.Data;

import java.sql.Date;

@Data
public class UserForm {
    private String name;
    private String nickname;
    private String userId;      // id 대신 loginId로 변경 (사용자 로그인용)
    private String password;
    private String email;
    private boolean gender;      // true = 남성, false = 여성
    private Date birth;
}
