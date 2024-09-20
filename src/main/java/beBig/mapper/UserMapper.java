package beBig.mapper;

import beBig.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper {
    void insert(UserVo userVo);
    UserVo findByUserId(String userId);
}
