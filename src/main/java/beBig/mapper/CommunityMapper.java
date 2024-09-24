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
    void insert(PostVo post);
    void updateLike(Long postId);
    void update(PostVo post);
    void delete(PostVo post);
}
