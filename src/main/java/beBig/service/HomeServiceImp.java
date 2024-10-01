package beBig.service;

import beBig.dto.*;
import beBig.mapper.AccountMapper;
import beBig.mapper.UserMapper;
import beBig.service.codef.CodefApiRequester;
import beBig.vo.AccountVo;
import beBig.vo.BankVo;
import beBig.vo.TransactionVo;
import beBig.vo.UserVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class HomeServiceImp implements HomeService {

    private final SqlSessionTemplate sqlSessionTemplate;
    private final CodefApiRequester codefApiRequester;

    @Override
    public UserVo getUserInfo(Long userId) throws Exception {
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        UserVo userInfo = userMapper.findByUserId(userId);

        if (userInfo == null) {
            log.warn("사용자 정보를 찾을 수 없습니다: {}", userId);
            throw new Exception("사용자 정보를 찾을 수 없습니다.");
        }

        log.info("사용자 정보 조회 성공: ID: {}, 이름: {}", userInfo.getUserId(), userInfo.getUserName());
        return userInfo;
    }

    @Override
    public List<CodefAccountDto> getUserAccount(Long userId, AccountDto accountDto) throws Exception {
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        UserVo userInfo = userMapper.findByUserId(userId);
        String connectedId = userInfo.getUserConnectedId();

        if (connectedId != null) {
            connectedId = codefApiRequester.addConnectedId(connectedId, accountDto);
        } else {
            connectedId = codefApiRequester.registerConnectedId(accountDto);
            userMapper.updateUserConnectedId(userId, connectedId);
        }

        return codefApiRequester.getAccountInfo(accountDto, connectedId);
    }

    @Override
    public boolean addAccountToDB(Long userId, List<CodefAccountDto> codefAccountDtoList) {
        AccountMapper accountMapper = sqlSessionTemplate.getMapper(AccountMapper.class);

        for (CodefAccountDto accountInfo : codefAccountDtoList) {
            AccountVo accountVo = mapToAccountVo(userId, accountInfo);
            accountMapper.insertAccount(accountVo);
            log.info("계좌 등록 완료: {}", accountVo.getAccountNum());
        }
        return true;
    }

    private AccountVo mapToAccountVo(Long userId, CodefAccountDto accountInfo) {
        AccountVo accountVo = new AccountVo();
        accountVo.setAccountNum(accountInfo.getResAccount());
        accountVo.setBankId(accountInfo.getBankVo().getBankId());
        accountVo.setAccountName(accountInfo.getResAccountName());
        accountVo.setAccountType(accountInfo.getResAccountDeposit());
        accountVo.setUserId(userId);
        return accountVo;
    }

    @Override
    public void saveTransactions(Long userId, CodefTransactionRequestDto requestDto) throws Exception {
        AccountMapper accountMapper = sqlSessionTemplate.getMapper(AccountMapper.class);
        CodefTransactionResponseDto responseDto = codefApiRequester.getTransactionHistory(requestDto);
        List<CodefTransactionResponseDto.HistoryItem> transactionHistory = responseDto.getResTrHistoryList();

        if (transactionHistory == null || transactionHistory.isEmpty()) {
            log.info("거래 내역이 없습니다: 계좌 번호 {}", requestDto.getAccount());
            return;
        }

        List<TransactionVo> transactionList = mapToTransactionList(requestDto, transactionHistory);

        for (TransactionVo transactionVo : transactionList) {
            accountMapper.insertTransaction(transactionVo);
        }
    }

    private List<TransactionVo> mapToTransactionList(CodefTransactionRequestDto requestDto, List<CodefTransactionResponseDto.HistoryItem> transactionHistory) throws Exception {
        List<TransactionVo> transactionList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmmss");

        for (CodefTransactionResponseDto.HistoryItem historyItem : transactionHistory) {
            TransactionVo transactionVo = new TransactionVo();
            transactionVo.setAccountNum(requestDto.getAccount());
            transactionVo.setTransactionVendor(historyItem.getResAccountDesc3());
            transactionVo.setTransactionBalance(Integer.parseInt(historyItem.getResAfterTranBalance()));

            int transactionOut = Integer.parseInt(historyItem.getResAccountOut());
            int transactionIn = Integer.parseInt(historyItem.getResAccountIn());
            transactionVo.setTransactionAmount(transactionOut != 0 ? transactionOut : transactionIn);

            Date transactionDate = dateFormat.parse(historyItem.getResAccountTrDate() + " " + historyItem.getResAccountTrTime());
            transactionVo.setTransactionDate(transactionDate);
            transactionVo.setTransactionType(transactionOut != 0 ? "출금" : "입금");

            transactionList.add(transactionVo);
        }
        return transactionList;
    }

    @Override
    public AccountTransactionDto getTransactionList(Long userId, String accountNum) {
        AccountMapper accountMapper = sqlSessionTemplate.getMapper(AccountMapper.class);

        // accountNum으로 계좌 정보 조회
        AccountVo accountVo = accountMapper.findAccountByAccountNum(accountNum);
        if (accountVo == null || !accountVo.getUserId().equals(userId)) {
            throw new IllegalArgumentException("유효하지 않은 계좌 번호입니다.");
        }

        // 거래 내역 조회
        List<TransactionVo> transactionList = accountMapper.getTransactionsByAccountNum(accountNum);


        // bankId로 은행 정보 조회
        BankVo bankVo = accountMapper.getBankById(accountVo.getBankId());

        // DTO 구성
        AccountTransactionDto response = new AccountTransactionDto();
        response.setTransactions(transactionList);
        response.setAccountName(accountVo.getAccountName());
        response.setBankName(bankVo.getBankName());

        return response;
    }


    @Override
    public List<AccountVo> showMyAccount(Long userId) throws Exception {
        AccountMapper accountMapper = sqlSessionTemplate.getMapper(AccountMapper.class);
        List<AccountVo> accountList = accountMapper.findAccountById(userId);

        if (accountList == null || accountList.isEmpty()) {
            log.warn("사용자와 연결된 계좌가 없습니다: {}", userId);
            throw new Exception("사용자와 연결된 계좌가 없습니다.");
        }

        log.info("계좌 목록 조회 성공: 사용자 ID: {}, 계좌 수: {}", userId, accountList.size());
        return accountList;
    }
}
