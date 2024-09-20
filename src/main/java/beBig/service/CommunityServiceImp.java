package beBig.service;

import beBig.mapper.CommunityMapper;
import beBig.vo.PostVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommunityServiceImp implements CommunityService {

    @Autowired
    private CommunityService communityService;


    @Override
    public List<PostVo> showList() {
        return List.of();
    }

    @Override
    public PostVo showDetail(int postId) {
        return null;
    }

    @Override
    public void write(PostVo post) {

    }

    @Override
    public void updateLike(int postId) {

    }

    @Override
    public void update(PostVo post) {

    }

    @Override
    public void delete(int postId) {

    }
}
