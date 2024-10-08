package beBig.mapper;

import beBig.vo.CodefTokenVo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CodefTokenMapper {
    CodefTokenVo getLatestToken();
    void insertToken(CodefTokenVo token);
    void updateToken(CodefTokenVo token);
}
