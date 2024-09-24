package beBig.service;

import beBig.dto.LikeRequestDto;
import beBig.vo.PostVo;

import java.util.List;

public interface CommunityService {
    public List<PostVo> showList(int postCategory, int postWriterFinTypeCode);
    public PostVo showDetail(long postId);
    public void write(PostVo post);
    public void updateLike(long userId, long postId);
    public void update(PostVo post);
    public void delete(Long postId);
}
