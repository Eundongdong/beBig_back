package beBig.mapper;

import beBig.vo.FinTestVo;
import beBig.vo.FinTypeVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HomeMapper {
    List<FinTestVo> findFinTest();

    FinTypeVo findFinTypeByUserId(long userId);
}
