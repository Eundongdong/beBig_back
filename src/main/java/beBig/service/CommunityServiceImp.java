package beBig.service;

import beBig.mapper.CommunityMapper;
import beBig.vo.PostVo;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CommunityServiceImp implements CommunityService {
    private SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    private CommunityServiceImp(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }


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
        //mapper 연결
        CommunityMapper communityMapper = sqlSessionTemplate.getMapper(CommunityMapper.class);
        try{
            communityMapper.insert(post);
        }catch (Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
        }
        log.info("post.toString()",post.toString());
        //아이디 어캐하지
        //s3에 올리기
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
