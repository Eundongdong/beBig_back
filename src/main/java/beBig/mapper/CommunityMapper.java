package beBig.mapper;

import beBig.vo.PostVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommunityMapper {
    List<PostVo> findAll();
    PostVo findDetail();
    void insert(PostVo post);
    void updateLike(int postId);
    void update(PostVo post);
    void delete(PostVo post);
}
