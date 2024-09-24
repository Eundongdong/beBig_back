package beBig.service;

import beBig.dto.LikeRequestDto;
import beBig.mapper.CommunityMapper;
import beBig.vo.PostVo;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CommunityServiceImp implements CommunityService {
    private SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    private CommunityServiceImp(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }


    @Override
    public List<PostVo> showList(int postCategory, int postWriterFinTypeCode) {
        CommunityMapper mapper = sqlSessionTemplate.getMapper(CommunityMapper.class);

        Map<String, Object> params = new HashMap<>();
        if(postCategory != -1){
            params.put("postCategory", postCategory);
        }
        if(postWriterFinTypeCode != -1){
            params.put("postWriterFinTypeCode", postWriterFinTypeCode);
        }
        // 전체 목록 조회(파라미터에 검색 필터가 없는 경우)
        if (params.isEmpty()) {
            return mapper.findAll();
        }
        // 카테고리/유형별 조회(파라미터 검색 필터가 있는 경우)
        else {
            return mapper.findByPostCategoryAndFinTypeCode(params);
        }
    }

    @Override
    public PostVo showDetail(long postId) {
        CommunityMapper mapper = sqlSessionTemplate.getMapper(CommunityMapper.class);
        PostVo detail = mapper.findDetail(postId);
        return detail;
    }

    @Override
    public void write(PostVo post) {

    }

    // 좋아요/좋아요 취소 처리

    @Override
    public void updateLike(long postWriterNo, long postId) {
        CommunityMapper mapper = sqlSessionTemplate.getMapper(CommunityMapper.class);
        Map<String, Object> params = new HashMap<>();
        params.put("postWriterNo", postWriterNo);
        params.put("postId", postId);

        // 좋아요 눌렀는지 체크
        int likeCnt = mapper.checkLike(params);

        // 이미 좋아요를 눌렀다면 좋아요 취소
        if(likeCnt > 0){
            mapper.removeLike(params);
            // 좋아요 수 감소
            params.put("likeCnt", -1);
        }
        // 좋아요를 누르지 않았다면 좋아요 추가
        else {
            mapper.addLike(params);
            // 좋아요 수 증가
            params.put("likeCnt", 1);
        }
        // 좋아요 수 없데이트
        mapper.updateLike(params);
    }

    @Override
    public void update(PostVo post) {

    }

    @Override
    public void delete(Long postId) {

    }
}
