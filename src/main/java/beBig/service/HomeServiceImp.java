package beBig.service;

import beBig.mapper.UserMapper;
import beBig.vo.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HomeServiceImp implements HomeService {
    private final SqlSessionTemplate sqlSessionTemplate;

    public HomeServiceImp(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }
    // 사용자 정보 불러오기
    @Override
    public UserVo getUserInfo(Long userId) throws Exception {
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);

        // userId로 사용자 정보를 가져옴
        UserVo userInfo = userMapper.findByUserId(userId);

        if (userInfo == null) {
            log.warn("사용자 정보를 찾을 수 없습니다: {}", userId);
            throw new Exception("사용자 정보를 찾을 수 없습니다.");
        }

        log.info("사용자 정보 조회 성공: {}", userInfo.getUserName());
        return userInfo;
    }
}
