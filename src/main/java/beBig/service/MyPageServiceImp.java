package beBig.service;

import beBig.dto.response.MyPagePostResponseDto;
import beBig.dto.response.UserProfileResponseDto;
import beBig.mapper.MissionMapper;
import beBig.mapper.UserMapper;
import beBig.vo.UserProfileResponseVo;
import beBig.mapper.MyPageMapper;
import beBig.vo.UserRankVo;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MyPageServiceImp implements MyPageService {
    private final SqlSessionTemplate sqlSessionTemplate;
    private final MyPageMapper myPageMapper;
    private final MissionMapper missionMapper;
    private final UserMapper userMapper;

    public MyPageServiceImp(SqlSessionTemplate sqlSessionTemplate, MyPageMapper myPageMapper, MissionMapper missionMapper, UserMapper userMapper) {
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.myPageMapper = myPageMapper;
        this.missionMapper = missionMapper;
        this.userMapper = userMapper;
    }

    @Override
    public UserProfileResponseDto findProfileByUserId(long userId) {
        UserProfileResponseVo userProfileResponseVo = myPageMapper.findUserProfile(userId);
        List<UserRankVo> list = myPageMapper.findUserRank();
        UserProfileResponseDto dto = new UserProfileResponseDto();

        dto.setBadgeCode(userProfileResponseVo.getBadgeCode());
        dto.setUserIntro(userProfileResponseVo.getUserIntro());
        dto.setUserNickname(userProfileResponseVo.getUserNickname());
        dto.setUserVisibility(userProfileResponseVo.getUserVisibility());
        dto.setFinTypeCode(userProfileResponseVo.getFinTypeCode());
        dto.setFinTypeInfo(userProfileResponseVo.getFinTypeInfo());

        long rank = 0;
        for (UserRankVo u : list) {
            if (u.getUserId() == userId) {
                rank = u.getUserRank();
                break;
            }
        }

        double percentageRank = (double) rank / list.size() * 100;
        dto.setUserRank((long) percentageRank);

        return dto;
    }

    @Override
    public List<MyPagePostResponseDto> findMyPostByUserId(long userId) {
        return myPageMapper.findMyPagePost(userId);
    }

    @Override
    public List<MyPagePostResponseDto> findMyLikeHitsByUserId(long userId) {
        return myPageMapper.findMyPageLikeHits(userId);
    }

}
