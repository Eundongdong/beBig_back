package beBig.mapper;


import beBig.vo.RefreshTokenVo;
import org.mapstruct.Mapper;

@Mapper
public interface RefreshTokenMapper {

    // Refresh Token 저장
    void saveRefreshToken(RefreshTokenVo refreshToken);

    // Refresh Token 찾기
    RefreshTokenVo findByToken(String token);

    // Refresh Token 삭제
    void deleteByToken(String token);

    // 특정 유저의 Refresh Token 삭제 (로그아웃 시)
    void deleteByUserId(Long userId);
}
