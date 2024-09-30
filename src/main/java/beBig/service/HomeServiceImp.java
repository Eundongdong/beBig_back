package beBig.service;

import beBig.form.AccountForm;
import beBig.form.CodefResponseForm;
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
     * @param accountForm
     * @return
     * @throws Exception
     */
    @Override
    public List<CodefResponseForm> getUserAccount(Long userId, AccountForm accountForm) throws Exception {
        // 사용자의 정보 및 기존 커넥티드 아이디 확인
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        UserVo userInfo = userMapper.findByUserId(userId);
        String connectedId = userInfo.getUserConnectedId();

        // 기존 커넥티드 아이디가 존재하는 경우
        if (connectedId != null) {
            // connectedId 와 accountList(은행, id, pw + 국가코드, 업무구분, 고객구분, 로그인방식 포함해서)
            // CodefApiRequester로 넘겨서 요청
            connectedId = codefApiRequester.addConnectedId(connectedId, accountForm);
        } else {
            // 없다면, accountList(은행, id, pw + 국가코드, 업무구분, 고객구분, 로그인방식 포함해서)
            // CodefApiRequester로 넘겨서 요청
            connectedId = codefApiRequester.registerConnectedId(accountForm);

            // UserMapper로 connectedId 업데이트
            userMapper.updateUserConnectedId(userId, connectedId); // 여기에 데이터베이스 업데이트 호출
        }

        // Codef API로부터 계좌 정보 요청
        return codefApiRequester.getAccountInfo(accountForm, connectedId);
    }


    @Override
    public boolean addAccountToDB(Long userId, List<CodefResponseForm> codefResponseFormList) {
        // CodefResponseForm 리스트를 순회하며 각 계좌를 DB에 저장
        for (CodefResponseForm accountInfo : codefResponseFormList) {
            // DB에 계좌 추가 로직
            // 계좌 정보는 accountInfo를 통해 접근할 수 있음
            // 예: saveAccountToDatabase(userId, accountInfo);
        }

        // 모든 계좌가 정상적으로 추가되었는지 확인 후 반환
        return true; // 또는 false로 변경
    }

    // 0927 end ------------------------------------------------

    @Override
    public List<AccountVo> showMyAccount(Long userId) throws Exception {
        UserVo userInfo = getUserInfo(userId); // 사용자 정보 조회

        // UserVo가 null인 경우 예외 처리
        if (userInfo == null) {
            throw new Exception("사용자 정보를 찾을 수 없습니다.");
        }

        String connectedId = userInfo.getUserConnectedId(); // connectedId 가져오기

        // *********** Codef API 연결여부에 따라 확인해야함 ******************

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
}
