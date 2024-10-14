package beBig.service;

import beBig.dto.response.MyPageEditResponseDto;
import beBig.dto.response.MyPagePostResponseDto;
import beBig.dto.response.UserProfileResponseDto;
import beBig.mapper.MissionMapper;
import beBig.mapper.UserMapper;
import beBig.vo.BadgeVo;
import beBig.vo.UserProfileResponseVo;
import beBig.mapper.MyPageMapper;
import beBig.vo.UserRankVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.xerces.impl.dv.util.Base64;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MyPageServiceImp implements MyPageService {
    private final SqlSessionTemplate sqlSessionTemplate;
    private final MyPageMapper myPageMapper;
    private final MissionMapper missionMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public MyPageServiceImp(SqlSessionTemplate sqlSessionTemplate, MyPageMapper myPageMapper, MissionMapper missionMapper, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.myPageMapper = myPageMapper;
        this.missionMapper = missionMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<BadgeVo> getBadges() {
        return myPageMapper.getBadges();
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
        int count = 0;
        for (UserRankVo u : list) {
            count++;
            if (u.getUserId() == userId) {
                rank = count;
                break;
            }
        }

        // 총 사용자 수 대비 백분율 계산 (소수점 포함)

        double percentageRank = ((double) rank / (double) list.size()) * 100;
        log.info("Calculated percentageRank: {}", percentageRank);  // 백분율 확인

        dto.setUserRank((long) Math.ceil(percentageRank));  // 소수점 반올림하여 DTO에 설정
        log.info("dto.UserRank (rounded percentage): {}", dto.getUserRank());  // 결과 로그 확인


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

    @Override
    public String findLoginIdByUserId(long userId) {
        return myPageMapper.findLoginIdByUserId(userId);
    }

    @Override
    public MyPageEditResponseDto findEditFormByUserId(long userId) {
        return myPageMapper.findEditDtoBy(userId);
    }

    @Override
    public void saveMyPageSocial(long userId, String userIntro, String userNickname) {
        myPageMapper.saveMyPageSocial(userId, userIntro, userNickname);
    }


    @Override
    public void saveMyPageGeneral(long userId, String userIntro, String userNickname, String password) {
        String encryptedPassword = passwordEncoder.encode(password);
        myPageMapper.saveMyPageGeneral(userId, userIntro, userNickname, encryptedPassword);
    }

    @Override
    public boolean checkPassword(String password, long userId) {
        String realPassword = myPageMapper.findPasswordByUserId(userId);
        log.info("realPassword : {}", realPassword);
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder.matches(password, realPassword);
    }

    @Override
    public void saveMyPageGeneralWithoutPassword(long userId, String userIntro, String userNickname) {
        myPageMapper.saveMyPageGeneralWithoutPassword(userId, userIntro, userNickname);
    }

    @Override
    public void updateVisibility(long userId, int userVisibility) {
        try {
            myPageMapper.updateVisibility(userId, userVisibility); // Mapper 호출하여 DB 업데이트
            log.info("User visibility updated for userId: {}, newVisibility: {}", userId, userVisibility);
        } catch (Exception e) {
            log.error("Error updating visibility for userId: {}", userId, e);
            throw new RuntimeException("Visibility update failed.");
        }
    }

}
