package beBig.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenVo {
    private Long id;
    private Long userId;
    private String token;
    private Date expiryDate;
}
