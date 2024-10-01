package beBig.service;

import beBig.dto.response.SpendingPatternsResponseDto;
import beBig.mapper.*;
import beBig.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AssetServiceImp implements AssetService {

    private SqlSession sqlSessionTemplate;

    @Autowired
    public void setSqlSessionTemplate(SqlSession sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    /**
     * TransactionVo에서 DTO 만드는 코드 
     * @param userId toekn으로 받아온 UserId
     * @param year : 조회할 년도
     * @return : 월별 누적합, 월평균, 현재 월과 이전 월 차이
     */
    @Override
    public SpendingPatternsResponseDto showSpendingPatterns(long userId,int year) {
        AssetMapper assetMapper = sqlSessionTemplate.getMapper(AssetMapper.class);

        // userId 로 해당하는 accountNum List로 불러오기
        List<String> accountNumList = assetMapper.findAccountNumByUserId(userId);
        log.info("accountNumList: {}", accountNumList);
        // 각 accountNum별로 TransactionVo List로 불러오기
        List<TransactionVo> transactionVoList = new ArrayList<>();
        for(String accountNum : accountNumList){
            List<TransactionVo> tmpTransactionVoList = assetMapper.findTransactionsByAccountNum(accountNum);
            transactionVoList.addAll(tmpTransactionVoList);
        }
        log.info("transactionVoList{}",transactionVoList);

        // Dto로 만들기
        SpendingPatternsResponseDto spendingPatternsResponseDto = calculateSpendingPatterns(transactionVoList,year);
        log.info("spendingPatternsResponseDto{}",spendingPatternsResponseDto);

        return spendingPatternsResponseDto;
    }

    public SpendingPatternsResponseDto calculateSpendingPatterns(List<TransactionVo> transactionVoList, int year) {
        // 날짜 포맷터 정의
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 현재 날짜 기준으로 연도와 월 정보를 가져옴
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonthValue = now.getMonthValue(); // 현재 월 (1 ~ 12)
        log.info("currentYear:{}, currentMonthValue:{}", currentYear, currentMonthValue);

        // 주어진 연도에 대해 1월부터 12월까지의 yyyy-MM 리스트 생성
        List<String> yearMonths = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            // 각 월을 yyyy-MM 형식으로 변환하여 리스트에 추가
            String month = String.format("%d-%02d", year, i);
            yearMonths.add(month);
        }

        // 월별로 그룹화하여 금액 합산 (해당 년도만 필터링)
        Map<String, Long> monthlySums = transactionVoList.stream()
                .filter(t -> {
                    // String을 LocalDate로 변환하여 해당 연도에 맞는 거래만 필터링
                    LocalDate date = LocalDate.parse(t.getTransactionDate(), formatter);
                    return date.getYear() == year;
                })
                .collect(Collectors.groupingBy(
                        t -> {
                            // yyyy-MM 형식으로 월 추출
                            LocalDate date = LocalDate.parse(t.getTransactionDate(), formatter);
                            return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                        },
                        Collectors.summingLong(TransactionVo::getTransactionAmount)
                ));

        // 1월부터 12월까지 순서대로 합계 리스트 생성 (없는 월은 0으로 채움)
        List<Long> monthlySumList = yearMonths.stream()
                .map(month -> monthlySums.getOrDefault(month, 0L)) // 해당 월의 합계가 없으면 0으로 채움
                .collect(Collectors.toList());

        // 미래의 월(현재 달보다 이후의 달)은 평균 계산에 포함하지 않음
        List<Long> validMonthlySums = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            int month = i + 1; // 1월부터 12월까지
            if (year < currentYear || (year == currentYear && month <= currentMonthValue)) {
                // 과거 또는 현재 달은 평균 계산에 포함
                validMonthlySums.add(monthlySumList.get(i));
            }
        }

        // 0을 포함한 월을 평균 계산에 포함 (미래의 월 제외)
        long monthlyAverage = 0;
        if (!validMonthlySums.isEmpty()) {
            monthlyAverage = validMonthlySums.stream().mapToLong(Long::longValue).sum() / validMonthlySums.size();
        }

        // 서버의 현재 달과 지난 달의 차이 계산 (해당 연도에 맞게 계산)
        String currentMonth = now.getYear() == year ? now.format(DateTimeFormatter.ofPattern("yyyy-MM")) : null;
        String previousMonth = now.getYear() == year ? now.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM")) : null;

        long previousMonthDiff = 0;
        if (currentMonth != null && previousMonth != null) {
            long currentMonthSum = monthlySums.getOrDefault(currentMonth, 0L);
            long previousMonthSum = monthlySums.getOrDefault(previousMonth, 0L);
            previousMonthDiff = currentMonthSum - previousMonthSum;
        }

        // DTO로 결과 반환
        return new SpendingPatternsResponseDto(monthlySumList, monthlyAverage, previousMonthDiff);
    }

    /**
     * 사용자 유형별 예/적금 상품 추천
     *
     * 1. 사용자의 주거래 은행을 찾아서 해당 은행에서 예/적금 추천 상품을 각 2개 추천
     * 2. 사용자의 다른 거래 은행들을 찾아서 해당 은행들에서도 예/적금 추천 상품을 각 2개씩 추천
     * 3. 사용자의 금융 유형(finTypeCode)을 기준으로
     *    - 유형 1 (꿀벌)과 3 (다람쥐)은 짧은 기간의 상품을 우선 추천
     *    - 유형 2 (호랑이)와 4 (나무늘보)는 긴 기간의 상품을 우선 추천
     *
     * @param userId 추천할 사용자의 고유 ID
     * @return 예/적금 추천 정보를 포함하는 Map 객체
     *         - "depositRecommendations": 예금 추천 상품 리스트 (최대 4개)
     *         - "savingsRecommendations": 적금 추천 상품 리스트 (최대 4개)
     */
    @Override
    public Map<String, Object> showProductRecommendations(long userId) {
        DepositProductMapper depositProductMapper = sqlSessionTemplate.getMapper(DepositProductMapper.class);
        SavingsProductMapper savingsProductMapper = sqlSessionTemplate.getMapper(SavingsProductMapper.class);
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        AccountMapper accountMapper = sqlSessionTemplate.getMapper(AccountMapper.class);

        UserVo userVo = userMapper.findByUserId(userId);
        if(userVo == null){
            // 유저 정보가 없으면 빈 맵 반환
            return new HashMap<>();
        }

        // 사용자 유형코드 추출
        int finTypeCode = userVo.getFinTypeCode();

        // 주거래 은행 조회(account 테이블 사용)
        int primaryBankId  = accountMapper.findPrimaryBankId(userId);

        // 다른 거래은행 조회
        Map<String, Object> paramsForOtherBanks = new HashMap<>();
        paramsForOtherBanks.put("userId", userId);
        paramsForOtherBanks.put("primaryBankId", primaryBankId);
        List<Integer> otherBankIds = accountMapper.findOtherBankIds(paramsForOtherBanks);

        // 예금 및 적금 추천 리스트 생성
        List<DepositProductVo> depositRecommendations = new ArrayList<>();
        List<SavingsProductVo> savingsRecommendations = new ArrayList<>();

        Map<String, Object> primaryBankParams = new HashMap<>();
        // 주거래은행에서 추천 상품 2개씩 가져오기
        primaryBankParams.put("bankId", primaryBankId);
        primaryBankParams.put("finTypeCode", finTypeCode);
        depositRecommendations.addAll(depositProductMapper.getTop2RecommendedDepositProducts(primaryBankParams));
        savingsRecommendations.addAll(savingsProductMapper.getTop2RecommendedSavingsProduct(primaryBankParams));

        // 다른 거래은행에서 각 은행별 추천 2개씩 가져오기
        for (int bankId : otherBankIds) {
            Map<String, Object> otherBankParams = new HashMap<>();
            otherBankParams.put("bankId", bankId);
            otherBankParams.put("finTypeCode", finTypeCode);
            depositRecommendations.addAll(depositProductMapper.getTop2RecommendedDepositProducts(otherBankParams));
            savingsRecommendations.addAll(savingsProductMapper.getTop2RecommendedSavingsProduct(otherBankParams));
            log.info("Other bankId : " + bankId);
        }

        // 은행별로 최상위 두 개의 예금 및 적금 추천 정보만 추출
        List<DepositProductVo> topDepositRecommendations = depositRecommendations.stream()
                .limit(4) // 주거래은행과 다른 은행에서 각 2개씩 총 4개 제한
                .collect(Collectors.toList());

        List<SavingsProductVo> topSavingsRecommendations = savingsRecommendations.stream()
                .limit(4) // 주거래은행과 다른 은행에서 각 2개씩 총 4개 제한
                .collect(Collectors.toList());

        Map<String, Object> recommendations = new HashMap<>();
        recommendations.put("depositRecommendations", topDepositRecommendations);
        recommendations.put("savingsRecommendations", topSavingsRecommendations);

        return recommendations;
    }
}
