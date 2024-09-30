package beBig.mapper;

import beBig.vo.DepositProductVo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepositApiMapper {
    void insert(DepositProductVo depositProductVo);
}
