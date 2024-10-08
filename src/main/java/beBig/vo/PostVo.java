package beBig.vo;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@NoArgsConstructor
@Getter @Setter
@AllArgsConstructor
@ToString
public class PostVo {
    private Long postId;
    private Long userId;
    private String userNickname;
    private String postTitle;
    private String postContent;
    private String postCreatedTime; // 추후 유틸 클래스를 통해 Date로 변환예정
    private String postUpdatedTime; // 추후 유틸 클래스를 통해 Date로 변환예정
    private int postLikeHits;
    private List<String> postImagePaths;
    private List<MultipartFile> files; //DTO : post로 요청받을 때 사용
    private int postCategory;
    private int finTypeCode; // DTO

    private List<LikeHitsVo> likeHits;  // DTO : 게시글에 달린 좋아요 목록

}
