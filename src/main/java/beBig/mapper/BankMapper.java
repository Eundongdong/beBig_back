package beBig.mapper;


import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BankMapper {
    Integer getBankIdByFssCode(String bankCodeFss);
}
