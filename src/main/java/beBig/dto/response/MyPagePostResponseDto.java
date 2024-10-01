package beBig.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyPagePostResponseDto {
    private String title;
    private Date postTime;
    private int postLikeHits;
}
