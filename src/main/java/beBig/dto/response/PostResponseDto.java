package beBig.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class PostResponseDto {
    private long postId;
    private long userId;
    private String postTitle;
    private String postContent;
    private String postCreatedTime;
    private String postUpdatedTime;
    private int postLikeHits;
    private int postCategory;
    private int finTypeCode;
    private List<String> postImagePaths;
    private boolean isUser;  // 사용자 인증 결과 (수정/삭제 권한 여부)
}
