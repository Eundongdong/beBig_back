package beBig.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ImageMapper {
    // 특정 게시글에 해당하는 이미지 목록 조회
    List<String> findByPostId(Long postId);

    // 이미지 경로로 이미지 조회
    String findByImagePath(String imagePath);

    // 이미지 추가
    void insertImage(long postId, String imagePath);

    // 이미지 경로로 이미지 삭제
    void deleteByImagePath(String imagePath);

    void deleteByPostId(Long postId);
}
