package beBig.service;

import beBig.mapper.CommunityMapper;
import beBig.vo.PostVo;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommunityServiceImp implements CommunityService {
    private SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    private CommunityServiceImp(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }


    @Override
    public List<PostVo> showList() {
        CommunityMapper mapper = sqlSessionTemplate.getMapper(CommunityMapper.class);
        List<PostVo> list = mapper.findAll();
        return list;
    }

    @Override
    public PostVo showDetail(Long postId) {
        CommunityMapper mapper = sqlSessionTemplate.getMapper(CommunityMapper.class);
        PostVo detail = mapper.findDetail(postId);
        return detail;
    }

    @Override
    public void write(PostVo post) {

    }

    @Override
    public void updateLike(Long postId) {

    }

    @Override
    public void update(PostVo post) {

    }

    @Override
    public void delete(Long postId) {

    }
}
