package beBig.mapper;

import beBig.vo.PostVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommunityMapper {
    List<PostVo> findAll();
    PostVo findDetail(Long postId);
    List<PostVo> findByPostCategoryAndFinTypeCode(Map<String, Object> params);
    long findPostCountByPostCategoryAndFinTypeCode(Map<String, Object> params);
    void insert(PostVo post);
    int findLikeHitsByPostId(Long postId);
    // 좋아요가 이미 눌린 상태인지 확인
    int checkLike(Map<String, Object> params);
    // 좋아요 추가
    void addLike(Map<String, Object> params);
    // 좋아요 취소
    void removeLike(Map<String, Object> params);
    // 좋아요 수 없데이트 매퍼
    void updateLike(Map<String, Object> params);
    // 좋아요 수 업데이트
    void updateLikeCnt(Long postId);
    void insertImage(PostVo post);
    void update(PostVo post);
    void delete(Long postId);
}
