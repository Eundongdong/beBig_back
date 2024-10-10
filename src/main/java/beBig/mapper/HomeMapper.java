package beBig.mapper;

import beBig.vo.FinTestVo;
import beBig.vo.FinTypeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomeMapper {
    List<FinTestVo> findFinTest();

    FinTypeVo findFinTypeByUserId(@Param("userId") long userId);

    void saveFinTypeWithUserId(@Param("userId") Long userId, @Param("userFinType") int userFinType, @Param("userIncome") int userIncome);
}
