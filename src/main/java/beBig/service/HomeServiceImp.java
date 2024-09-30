package beBig.service;

import beBig.dto.AccountDto;
import beBig.dto.CodefResponseDto;
import beBig.mapper.AccountMapper;
import beBig.mapper.UserMapper;
import beBig.service.codef.CodefApiRequester;
import beBig.vo.AccountVo;
import beBig.vo.UserVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HomeServiceImp implements HomeService {
    private final SqlSessionTemplate sqlSessionTemplate;
    private final CodefApiRequester codefApiRequester;

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
    // 0927 codefAPI start ------------------------------------------------

    /**
     * 사용자의 커넥티드아이디를 통해 해당 은행의 계좌 정보 요청
     *
     * @param userId
     * @param accountDto
     * @return
     * @throws Exception
     */
    @Override
    public List<CodefResponseDto> getUserAccount(Long userId, AccountDto accountDto) throws Exception {
        // 사용자의 정보 및 기존 커넥티드 아이디 확인
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        UserVo userInfo = userMapper.findByUserId(userId);
        String connectedId = userInfo.getUserConnectedId();

        // 기존 커넥티드 아이디가 존재하는 경우
        if (connectedId != null) {
            // connectedId 와 accountList(은행, id, pw + 국가코드, 업무구분, 고객구분, 로그인방식 포함해서)
            // CodefApiRequester로 넘겨서 요청
            connectedId = codefApiRequester.addConnectedId(connectedId, accountDto);
        } else {
            // 없다면, accountList(은행, id, pw + 국가코드, 업무구분, 고객구분, 로그인방식 포함해서)
            // CodefApiRequester로 넘겨서 요청
            connectedId = codefApiRequester.registerConnectedId(accountDto);

            // UserMapper로 connectedId 업데이트
            userMapper.updateUserConnectedId(userId, connectedId); // 여기에 데이터베이스 업데이트 호출
        }

        // Codef API로부터 계좌 정보 요청
        return codefApiRequester.getAccountInfo(accountDto, connectedId);
    }


    @Override
    public boolean addAccountToDB(Long userId, List<CodefResponseDto> codefResponseDtoList) {
        AccountMapper accountMapper = sqlSessionTemplate.getMapper(AccountMapper.class);
        // CodefResponseForm 리스트를 순회하며 각 계좌를 DB에 저장
        for (CodefResponseDto accountInfo : codefResponseDtoList) {
            AccountVo accountVo = new AccountVo();
            accountVo.setAccountNum(accountInfo.getResAccount());
            accountVo.setBankId(accountInfo.getBankVo().getBankId()); // 은행명 대신 은행 ID 사용
            accountVo.setAccountName(accountInfo.getResAccountName());
            accountVo.setAccountType(accountInfo.getResAccountDeposit());
            accountVo.setUserId(userId);

            // DB에 계좌 추가
            accountMapper.insertAccount(accountVo);
            log.info("addition complete" + accountVo.getAccountNum());
        }
        return true;
    }

    // 0927 end ------------------------------------------------

    @Override
    public List<AccountVo> showMyAccount(Long userId) throws Exception {
        // AccountMapper 호출하여 userId로 계좌 정보 조회
        AccountMapper accountMapper = sqlSessionTemplate.getMapper(AccountMapper.class);
        List<AccountVo> accountList = accountMapper.findAccountById(userId);

        // 계좌 정보가 없는 경우 예외 처리
        if (accountList == null || accountList.isEmpty()) {
            log.warn("사용자와 연결된 계좌가 없습니다: {}", userId);
            throw new Exception("사용자와 연결된 계좌가 없습니다.");
        }

        log.info("사용자의 계좌 목록 조회 성공: {}", accountList.size());
        return accountList; // 계좌 목록 반환
    }
}
