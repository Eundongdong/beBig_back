package beBig.vo;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class CodefTokenVo {
    private Long id;
    private String accessToken;
    private Timestamp tokenExpiryTime;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
