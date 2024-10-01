package beBig.service;

import beBig.dto.UserTotalAssetsDto;
import beBig.dto.response.SpendingPatternsResponseDto;
import beBig.mapper.AssetMapper;
import beBig.mapper.UserMapper;
import beBig.vo.TransactionVo;
import beBig.vo.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
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
        log.info("now: {}", now);
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
     * 같은 나잇대 유저들의 총 자산을 불러오고, UserId가 몇 등인지 반환
     * 1. userId와 같은 나잇대 유저 List
     * 2. 각 유저별로 계좌 List 찾기 + 각 유저의 총 자산 구하기
     * 3. 같은 나잇대 유저의 총 자산을 기준으로 랭킹 부여하기
     * 4. userId가 몇등인지 반환하기
     * @param userId : 조회할 아이디
     * @return UserTotalAssetsDto
     */
    @Override
    public UserTotalAssetsDto showAgeComparison(long userId) {
        LocalDate now = LocalDate.now();
        AssetMapper assetMapper = sqlSessionTemplate.getMapper(AssetMapper.class);
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);

        // userId와 같은 나잇대의 유저 리스트 가져오기
        List<UserVo> sameAgeUserList = userMapper.findBySameAgeRange(userId);
        log.info("sameAgeUserList{}", sameAgeUserList);

        // UserId별로 계좌 리스트 받아오기 및 총 자산 계산
        List<UserTotalAssetsDto> totalAssetsDtoList = new ArrayList<>();
        Map<Long, UserTotalAssetsDto> totalAssetsDtoMap = new HashMap<>();  // userId로 빠른 접근을 위한 Map
        for (UserVo user : sameAgeUserList) {
            UserTotalAssetsDto totalAssetsDto = new UserTotalAssetsDto();
            totalAssetsDto.setAgeRange(user.getUserAgeRange());
            totalAssetsDto.setUserId(user.getUserId());
            totalAssetsDto.setAge(now.getYear() - user.getUserBirth().toInstant()
                    .atZone(ZoneId.systemDefault()) // 시스템 기본 시간대 사용
                    .toLocalDate().getYear() + 1);

            // 유저의 계좌 정보 불러오기
            List<String> accountNumList = assetMapper.findAccountNumByUserId(user.getUserId());
            long totalAsset = 0;
            if (!accountNumList.isEmpty()) {
                // 계좌 정보로 총 자산 계산
                totalAsset = assetMapper.findTotalAssetsByAccountNum(accountNumList);
            }
            totalAssetsDto.setTotalAssets(totalAsset);

            // 리스트와 맵에 각각 저장
            totalAssetsDtoList.add(totalAssetsDto);
            totalAssetsDtoMap.put(user.getUserId(), totalAssetsDto);
        }

        // 총 자산을 기준으로 내림차순 정렬 후 순위 부여
        totalAssetsDtoList.sort(Comparator.comparingDouble(UserTotalAssetsDto::getTotalAssets).reversed());
        log.info("totalAssetsDtoList{}", totalAssetsDtoList);

        // Rank 부여
        int rank = 1;
        for (UserTotalAssetsDto dto : totalAssetsDtoList) {
            dto.setRank(rank++);
        }

        // 유저찾기
        UserTotalAssetsDto targetUser = totalAssetsDtoMap.get(userId);
        targetUser.setTotalSameAgeRangeUsers(totalAssetsDtoList.size());

        return targetUser;
    }
}
