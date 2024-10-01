package beBig.service;


import beBig.dto.response.MyPageEditResponseDto;
import beBig.dto.response.MyPagePostResponseDto;
import beBig.dto.response.UserProfileResponseDto;

import java.util.List;

public interface MyPageService {
    UserProfileResponseDto findProfileByUserId(long userId);

    List<MyPagePostResponseDto> findMyPostByUserId(long userId);

    List<MyPagePostResponseDto> findMyLikeHitsByUserId(long userId);

    String findLoginIdByUserId(long userId);

    MyPageEditResponseDto findEditFormByUserId(long userId);

    void saveMyPageSocial(long userId, String userIntro, String userNickname);

    void saveMyPageGeneral(long userId, String userIntro, String userNickname, String password);
}
