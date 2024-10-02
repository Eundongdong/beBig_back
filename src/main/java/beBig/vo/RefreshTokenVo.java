package beBig.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenVo {
    private Long id;               // 고유 ID
    private Long userId;           // 사용자 ID
    private String refreshToken;   // Refresh Token 값
    private Date expiryDate;       // 만료 시간
}
