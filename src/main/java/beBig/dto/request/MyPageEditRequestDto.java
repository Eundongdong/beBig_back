package beBig.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyPageEditRequestDto {
    private String userIntro;
    private String userNickname;
    private String userName;
    private String userLoginId;
    private String userEmail;
    private int userGender;
    private Date userBirth;
    private String loginType;
    private String password;
}
