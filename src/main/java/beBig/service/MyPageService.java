package beBig.service;


import beBig.dto.response.MyPagePostResponseDto;
import beBig.dto.response.UserProfileResponseDto;

import java.util.List;

public interface MyPageService {
    UserProfileResponseDto findProfileByUserId(long userId);

    List<MyPagePostResponseDto> findMyPostByUserId(long userId);

    List<MyPagePostResponseDto> findMyLikeHitsByUserId(long userId);
}
