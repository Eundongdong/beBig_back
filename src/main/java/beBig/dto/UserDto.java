package beBig.dto;

import lombok.Data;

import java.sql.Date;

@Data
public class UserDto {
    private String name;
    private String nickname;
    private String userLoginId;      //사용자로그인용
    private String password;
    private String email;
    private boolean gender;      // true = 남성, false = 여성
    private Date birth;
    private String userLoginType;
}
