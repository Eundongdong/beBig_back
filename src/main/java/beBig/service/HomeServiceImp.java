package beBig.service;

import beBig.dto.*;
import beBig.dto.response.FinInfoResponseDto;
import beBig.mapper.AccountMapper;
import beBig.mapper.HomeMapper;
import beBig.mapper.UserMapper;
import beBig.service.codef.CodefApiRequester;
import beBig.vo.AccountVo;
import beBig.vo.BankVo;
import beBig.vo.TransactionVo;
import beBig.vo.FinTestVo;
import beBig.vo.FinTypeVo;
import beBig.vo.UserVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HomeServiceImp implements HomeService {

    private final MissionService missionService;
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
    public List<CodefAccountDto> addAccount(Long userId, AccountRequestDto accountRequestDto) throws Exception {
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        AccountMapper accountMapper = sqlSessionTemplate.getMapper(AccountMapper.class);

        // 1. 유저 정보 가져오기 및 connectedId 확인
        UserVo userInfo = userMapper.findByUserId(userId);
        String connectedId = userInfo.getUserConnectedId();

        // 2. connectedId 처리 (연결 아이디가 없을 경우 등록)
        if (connectedId == null) {
            connectedId = codefApiRequester.registerConnectedId(accountRequestDto);
            userMapper.updateUserConnectedId(userId, connectedId);
        } else {
            connectedId = codefApiRequester.addConnectedId(connectedId, accountRequestDto);
        }

        // 3. 계좌 정보 가져오기
        List<CodefAccountDto> accountList = codefApiRequester.getAccountInfo(accountRequestDto, connectedId);

        // 4. 계좌 정보가 없으면 예외 처리
        if (accountList == null || accountList.isEmpty()) {
            log.info("등록된 계좌가 없습니다.");
            throw new Exception("아이디/비밀번호를 확인하세요.");
        }

        // 5. 계좌 정보를 AccountVo 리스트로 매핑
        List<AccountVo> accountsToInsert = accountList.stream()
                .map(accountInfo -> mapToAccountVo(userId, accountInfo))
                .toList();

        // 6. 배치로 계좌 저장
        accountMapper.insertAccount(accountsToInsert);
        log.info("{}개의 계좌가 등록되었습니다.", accountsToInsert.size());

        // 7. 거래 내역 저장 및 임의의 거래 내역 생성 처리
        for (CodefAccountDto accountInfo : accountList) {
            boolean hasTransactions = saveTransactions(userId, accountInfo.getResAccount(), 360);

            if (!hasTransactions) {
                log.info("거래 내역이 없으므로 현재 잔액으로 임의의 거래 내역 생성");
                createDummyTransaction(accountInfo);
            }
        }

        // 6. 일일 미션 추가
        missionService.addDailyMissions(userId);

        // 7. 저장된 계좌 정보 반환
        return accountList;
    }

    // 거래 내역이 없을 경우 임의의 거래 내역을 생성하는 메서드
    private void createDummyTransaction(CodefAccountDto accountInfo) {
        AccountMapper accountMapper = sqlSessionTemplate.getMapper(AccountMapper.class);

        // 임의의 거래 내역을 담을 리스트 생성
        List<TransactionVo> dummyTransactions = new ArrayList<>();

        // 임의의 거래 내역 생성
        TransactionVo transactionVo = new TransactionVo();
        transactionVo.setAccountNum(accountInfo.getResAccount());
        transactionVo.setTransactionBalance(Integer.parseInt(accountInfo.getResAccountBalance()));
        transactionVo.setTransactionAmount(Integer.parseInt(accountInfo.getResAccountBalance()));
        transactionVo.setTransactionDate(new Date());
        transactionVo.setTransactionType("입금");
        transactionVo.setTransactionVendor("잔액 조회");

        // 리스트에 거래 내역 추가
        dummyTransactions.add(transactionVo);

        // 배치로 거래 내역 저장
        accountMapper.insertTransactionBatch(dummyTransactions); // 임의의 거래 내역 저장
        log.info("임의의 거래 내역 등록 완료: 계좌 번호 {}, 잔액 {}", transactionVo.getAccountNum(), transactionVo.getTransactionBalance());
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

    public boolean saveTransactions(Long userId, String accountNum, int days) throws Exception {
        AccountMapper accountMapper = sqlSessionTemplate.getMapper(AccountMapper.class);

        // 1. 계좌번호로 bankId와 bankCode 조회
        int bankId = accountMapper.findBankIdByAccountNum(accountNum);
        String bankCode = accountMapper.findBankCodeByBankId(bankId);

        // 거래 내역 요청 객체 생성
        CodefTransactionRequestDto requestDto = new CodefTransactionRequestDto();
        UserVo userInfo = getUserInfo(userId);
        requestDto.setAccount(accountNum);
        requestDto.setConnectedId(userInfo.getUserConnectedId());
        requestDto.setOrganization(bankCode);

        // 날짜 설정 (최근 {days}일)
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        requestDto.setStartDate(startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        requestDto.setEndDate(endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        requestDto.setOrderBy("0");

        log.info(requestDto.toString());

        // 2. 거래 내역 조회
        CodefTransactionResponseDto responseDto = codefApiRequester.getTransactionHistory(requestDto);
        if (responseDto == null) {
            return false;
        }
        List<CodefTransactionResponseDto.HistoryItem> transactionHistory = responseDto.getResTrHistoryList();
        List<TransactionVo> newTransactionList = mapToTransactionList(requestDto, transactionHistory);

        // 3. 최근 이틀치 거래 내역 조회 (최적화된 쿼리 수행)
        List<TransactionVo> existingTransactions = accountMapper.findTransactionsByAccountNumAndDateRange(
                accountNum, startDate, endDate);

        if (existingTransactions.isEmpty()) {
            // 기존 거래가 없으면 모든 거래를 저장
            saveTransactionsInBatch(newTransactionList);
            log.info("모든 거래 내역이 저장되었습니다.");
            return true;
        }

        // 4. 기존 거래 내역을 HashSet에 저장 (중복 체크용 키 생성)
        Set<String> existingTransactionKeys = existingTransactions.stream()
                .map(tx -> tx.getTransactionDate() + "_" + tx.getTransactionAmount())
                .collect(Collectors.toSet());

        // 5. 중복되지 않는 거래만 필터링
        List<TransactionVo> transactionsToSave = newTransactionList.stream()
                .filter(tx -> !existingTransactionKeys.contains(tx.getTransactionDate() + "_" + tx.getTransactionAmount()))
                .toList();

        // 6. 새로운 거래 내역이 있다면 저장
        if (!transactionsToSave.isEmpty()) {
            saveTransactionsInBatch(transactionsToSave);
            log.info("새로운 거래 내역이 저장되었습니다.");
        } else {
            log.info("저장할 새로운 거래 내역이 없습니다.");
            return false;
        }

        return true;
    }

    private void saveTransactionsInBatch(List<TransactionVo> transactions) {
        AccountMapper accountMapper = sqlSessionTemplate.getMapper(AccountMapper.class);
        accountMapper.insertTransactionBatch(transactions);  // 배치 삽입 호출
        log.info("배치로 {}개의 거래 내역 저장 완료", transactions.size());
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
    public AccountTransactionDto getTransactionList(Long userId, String accountNum, int page, int pageSize) {
        AccountMapper accountMapper = sqlSessionTemplate.getMapper(AccountMapper.class);

        // accountNum으로 계좌 정보 조회
        AccountVo accountVo = accountMapper.findAccountByAccountNum(accountNum);
        if (accountVo == null || !accountVo.getUserId().equals(userId)) {
            throw new IllegalArgumentException("유효하지 않은 계좌 번호입니다.");
        }

        int limit = pageSize;
        int offset = page * pageSize;
        // 거래 내역 조회
        List<TransactionVo> transactionList = accountMapper.getTransactionsByAccountNum(accountNum, limit, offset);

        // bankId로 은행 정보 조회
        BankVo bankVo = accountMapper.getBankById(accountVo.getBankId());

        // 총 페이지 구하기
        long totalPosts = accountMapper.getTransactionCountByAccountNum(accountNum);
        long totalPages = (long) Math.ceil((double) totalPosts / pageSize);

        // DTO 구성
        AccountTransactionDto response = new AccountTransactionDto();
        response.setTransactions(transactionList);
        response.setAccountName(accountVo.getAccountName());
        response.setBankName(bankVo.getBankName());
        response.setTotalPage(totalPages);
        return response;
    }


    @Override
    public List<AccountResponseDto> showMyAccount(Long userId) throws Exception {
        AccountMapper accountMapper = sqlSessionTemplate.getMapper(AccountMapper.class);
        List<AccountResponseDto> accountDetails = accountMapper.findAccountDetailsByUserId(userId);

        if (accountDetails == null || accountDetails.isEmpty()) {
            log.warn("사용자와 연결된 계좌가 없습니다: {}", userId);
            throw new Exception("사용자와 연결된 계좌가 없습니다.");
        }

        log.info("계좌 목록 조회 성공: 사용자 ID: {}, 계좌 수: {}", userId, accountDetails.size());
        return accountDetails;
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
    public void saveUserFinType(Long userId, int userFinType, int userIncome) {
        HomeMapper homeMapper = sqlSessionTemplate.getMapper(HomeMapper.class);
        homeMapper.saveFinTypeWithUserId(userId, userFinType, userIncome);
        missionService.addDailyMissions(userId);
    }
}
