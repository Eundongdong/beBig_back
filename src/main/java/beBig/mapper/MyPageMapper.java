package beBig.mapper;

import beBig.dto.response.MyPagePostResponseDto;
import beBig.vo.UserProfileResponseVo;
import beBig.vo.UserRankVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MyPageMapper {
    UserProfileResponseVo findUserProfile(@Param("userId") long userId);

    List<UserRankVo> findUserRank();

    List<MyPagePostResponseDto> findMyPagePost(@Param("userId") long userId);

    List<MyPagePostResponseDto> findMyPageLikeHits(@Param("userId") long userId);
}
