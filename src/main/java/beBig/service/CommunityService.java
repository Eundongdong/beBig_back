package beBig.service;

import beBig.vo.PostVo;

import java.util.List;

public interface CommunityService {
    public List<PostVo> showList();
    public PostVo showDetail(Long postId);
    public void write(PostVo post);
    public void updateLike(Long postId);
    public void update(PostVo post);
    public void delete(Long postId);
}
