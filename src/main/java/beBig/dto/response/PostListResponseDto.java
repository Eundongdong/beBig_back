package beBig.dto.response;

import beBig.vo.PostVo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter @Setter
public class PostListResponseDto {
    List<PostVo> list;
    long totalPage;
}
