package beBig.service;

import beBig.dto.response.FinInfoResponseDto;
import beBig.mapper.AccountMapper;
import beBig.mapper.HomeMapper;
import beBig.mapper.UserMapper;
import beBig.vo.AccountVo;
import beBig.vo.FinTestVo;
import beBig.vo.FinTypeVo;
import beBig.vo.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class HomeServiceImp implements HomeService {
    private final SqlSessionTemplate sqlSessionTemplate;

    public HomeServiceImp(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    // 사용자 정보 불러오기
    @Override
    public UserVo getUserInfo(Long userId) throws Exception {
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);

        // userId로 사용자 정보를 가져옴
        UserVo userInfo = userMapper.findByUserId(userId);

        if (userInfo == null) {
            log.warn("사용자 정보를 찾을 수 없습니다: {}", userId);
            throw new Exception("사용자 정보를 찾을 수 없습니다.");
        }

        log.info("사용자 ID: {}, finTypeCode: {}", userId, userInfo.getFinTypeCode());
        log.info("사용자 정보 조회 성공: {}", userInfo.getUserName());

        return userInfo;
    }

    @Override
    public List<AccountVo> showMyAccount(Long userId) throws Exception {
        // UserVo 조회
        UserVo userInfo = getUserInfo(userId); // 사용자 정보 조회

        // UserVo가 null인 경우 예외 처리
        if (userInfo == null) {
            throw new Exception("사용자 정보를 찾을 수 없습니다.");
        }

        String connectedId = userInfo.getUserConnectedId(); // connectedId 가져오기

        // *********** 후에 Codef API 연결여부에 따라 확인해야함 ******************

        // AccountMapper 호출하여 connectedId로 계좌 정보 조회
        AccountMapper accountMapper = sqlSessionTemplate.getMapper(AccountMapper.class);
        List<AccountVo> accountList = accountMapper.findAccountById(connectedId);

        // 계좌 정보가 없는 경우 예외 처리
        if (accountList == null || accountList.isEmpty()) {
            log.warn("사용자와 연결된 계좌가 없습니다: {}", connectedId);
            throw new Exception("사용자와 연결된 계좌가 없습니다.");
        }

        log.info("사용자의 계좌 목록 조회 성공: {}", accountList.size());
        return accountList; // 계좌 목록 반환
    }

    @Override
    public List<FinTestVo> findMission() {
        HomeMapper homeMapper = sqlSessionTemplate.getMapper(HomeMapper.class);
        List<FinTestVo> list = homeMapper.findFinTest();
        return list;
    }

    @Override
    public FinInfoResponseDto findFinTypeByUserId(Long userId) {
        HomeMapper homeMapper = sqlSessionTemplate.getMapper(HomeMapper.class);
        FinTypeVo vo = homeMapper.findFinTypeByUserId(userId);
        FinInfoResponseDto type = new FinInfoResponseDto();

        type.setFinTypeAnimal(vo.getFinTypeAnimal());
        type.setFinTypeTitle(vo.getFinTypeTitle());
        type.setFinTypeHabit1(vo.getFinTypeHabit1());
        type.setFinTypeHabit2(vo.getFinTypeHabit2());
        type.setFinTypeAnimalDescription(vo.getFinTypeAnimalDescription());
        type.setFinTypeTitleDescription(vo.getFinTypeTitleDescription());
        type.setFinTypeCode(vo.getFinTypeCode());

        log.info("type : {}", type);

        return type;
    }

    @Override
    public void saveUserFinType(Long userId, int userFinType) {
        HomeMapper homeMapper = sqlSessionTemplate.getMapper(HomeMapper.class);
        homeMapper.saveFinTypeWithUserId(userId, userFinType);
    }
}
