package beBig.mapper;

import beBig.dto.SavingsProductDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SavingsProductMapper {
    void insertSavingsProduct(SavingsProductDto savingsProductDto);
}
