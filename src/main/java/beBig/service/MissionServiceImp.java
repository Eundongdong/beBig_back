package beBig.service;

import beBig.dto.response.DailyMissionResponseDto;
import beBig.dto.response.MonthlyMissionResponseDto;
import beBig.mapper.MissionMapper;
import beBig.vo.PersonalMonthlyMissionVo;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MissionServiceImp implements MissionService {
    private final SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    public MissionServiceImp(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    // 월간 미션 조회
    @Override
    public MonthlyMissionResponseDto showMonthlyMission(long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        return missionMapper.getPersonalMonthlyMission(userId);
    }

    // 일간 미션 조회
    @Override
    public List<DailyMissionResponseDto> showDailyMission(long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        return missionMapper.getPersonalDailyMission(userId);
    }

    // 월간 미션 완료 처리
    @Override
    public void completeMonthlyMission(long personalMissionId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        missionMapper.completeMonthlyMission(personalMissionId, 1);
    }

    // 일간 미션 완료 처리
    @Override
    public void completeDailyMission(long personalMissionId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        missionMapper.completeDailyMission(personalMissionId);
    }

    @Override
    public double findRate(long userId, long missionId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        int missionType = missionMapper.findMissionCategoryByMissionId(missionId);
        if (missionType == 2) {
            return missionMapper.findSaveRateByUserIdAndMissionId(userId, missionId);
        } else if (missionType == 3) {
            return missionMapper.findUseRateByUserIdAndMissionId(userId, missionId);
        }
        return 0.0;
    }

    // 사용자 급여 조회
    @Override
    public int findSalary(long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        return missionMapper.findSalaryByUserId(userId);
    }

    // 점수 계산 및 업데이트
    @Override
    public int updateScore(long userId, int amount) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        //월별점수가져옴
        int currentScore = missionMapper.findCurrentMissionMonthScoreByUserId(userId) + amount;
        missionMapper.updateCurrentMissionMonthScoreByUserId(userId, currentScore);
        return currentScore;
    }

    // 미션 완료 여부 조회
    @Override
    public long findIsCompleted(long personalMissionId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        return missionMapper.findMissionIsCompletedByPersonalMissionId(personalMissionId);
    }

    // 현재 월 점수 조회
    @Override
    public int findCurrentMonthScore(long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        return missionMapper.findCurrentMissionMonthScoreByUserId(userId);
    }

    // 전체 사용자 일간 미션 갱신
    public void assignDailyMission() {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        List<Long> userIds = missionMapper.findAllUsersWithDailyMissions();

        for (Long userId : userIds) {
            updateDailyMissionForUser(userId);
        }
    }

    // 사용자 미션 갱신
    public void updateDailyMissionForUser(Long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        List<Integer> dailyMissions = missionMapper.findRandomMissionsByType(2, 3);
        List<Integer> existingMissionIds = missionMapper.findExistingDailyMissions(userId);

        if (!existingMissionIds.isEmpty()) {
            for (int i = 0; i < Math.min(dailyMissions.size(), existingMissionIds.size()); i++) {
                missionMapper.updateDailyMission(userId, existingMissionIds.get(i), dailyMissions.get(i));
            }
        }
    }

    // 신규 일간 미션 추가
    public void addDailyMissions(Long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        boolean isAssetAndSurveyLoaded = missionMapper.countUserAssetStatus(userId) > 0;

        if (isAssetAndSurveyLoaded) {
            int existingMissionCount = missionMapper.findExistingDailyMissionsCount(userId);

            if (existingMissionCount == 0) {
                List<Integer> dailyMissions = missionMapper.findRandomMissionsByType(2, 3);
                for (int dailyMission : dailyMissions) {
                    missionMapper.insertDailyMission(userId, dailyMission);
                }
                generateMonthlyMission(userId);
            }
        }
    }

    // 신규 월간 미션 생성
    public void generateMonthlyMission(Long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        missionMapper.insertMonthlyMission(userId, 1);
        log.info("1번 미션 할당");
    }

    // 월간 미션 업데이트 - 월초 batch
    @Override
    public void updateMonthlyMissionForAllUsers() {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        List<Long> userIds = missionMapper.findAllUsersWithMonthlyMissions();

        for (Long userId : userIds) {
            int currentMissionId = missionMapper.getCurrentMonthlyMissionId(userId);
            int nextMissionId = (currentMissionId % 6) + 1;  // 미션 순환
            missionMapper.updateMonthlyMission(userId, nextMissionId);
            log.info(nextMissionId + "번 미션으로 업데이트");
        }
    }

    // 매일 체크하는 월간 미션 수행 확인 - 일간 batch
    public void dailyCheckMonthlyMissions() {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        List<Long> userIds = missionMapper.findAllUsersWithMonthlyMissions();

        for (Long userId : userIds) {
            PersonalMonthlyMissionVo currentMission = missionMapper.getCurrentMonthlyMission(userId);

            switch (currentMission.getMissionId()) {
                case 1:
//                    if (missionMapper.countSavingsAccounts(userId) > 0) {
//                        markMissionAsComplete(currentMission.getPersonalMonthlyMissionId());
//                    } else {
//                        markMissionAsInProgress(currentMission.getPersonalMonthlyMissionId());
//                    }
//                    break;
                    log.info("case 1 undefined yet...");
                    break;
                case 2:
                    if (missionMapper.countCommunityPosts(userId) >= 4) {
                        markMissionAsComplete(currentMission.getPersonalMonthlyMissionId());
                    } else {
                        markMissionAsInProgress(currentMission.getPersonalMonthlyMissionId());
                    }
                    break;
                case 3:
                    if (missionMapper.countPostLikesInMonth(userId) >= 50) {
                        markMissionAsComplete(currentMission.getPersonalMonthlyMissionId());
                    } else {
                        markMissionAsInProgress(currentMission.getPersonalMonthlyMissionId());
                    }
                    break;
                case 6:
                    if (missionMapper.countCompletedDailyMissions(userId) > 0) {
                        markMissionAsInProgress(currentMission.getPersonalMonthlyMissionId());
                    } else {
                        markMissionAsFailed(currentMission.getPersonalMonthlyMissionId());
                    }
                    break;
                default:
                    log.warn("Unknown daily mission ID: " + currentMission.getMissionId());
            }
        }
    }

    // n 값을 결정하는 메서드 -> ??????????
    public int determineNValue(Long userId) {
        return 0;
    }

    // 월말 체크하는 월간 미션 수행 확인  - 월말 batch
    public void checkEndOfMonthMissions() {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        List<Long> userIds = missionMapper.findAllUsersWithMonthlyMissions();

        for (Long userId : userIds) {
            int n = determineNValue(userId);
            PersonalMonthlyMissionVo currentMission = missionMapper.getCurrentMonthlyMission(userId);

            switch (currentMission.getMissionId()) {
                case 4:
                    if (missionMapper.calculateSpendingDifference(userId) < n) {
                        markMissionAsComplete(currentMission.getPersonalMonthlyMissionId());
                    } else {
                        markMissionAsFailed(currentMission.getPersonalMonthlyMissionId());
                    }
                    break;
                case 5:
                    int savingDifference = missionMapper.calculateSavingDifference(userId);
                    if (savingDifference >= n) {
                        markMissionAsComplete(currentMission.getPersonalMonthlyMissionId());
                    } else {
                        markMissionAsFailed(currentMission.getPersonalMonthlyMissionId());
                    }
                    break;

                default:
                    log.warn("Unknown end of month mission ID: " + currentMission.getMissionId());
            }
        }
    }


    // 상태 업데이트 메서드들
    public void markMissionAsInProgress(int personalMissionId) {
        updateMissionStatus(personalMissionId, 0); // 진행 중 상태
    }

    public void markMissionAsComplete(int personalMissionId) {
        updateMissionStatus(personalMissionId, 1); // 완료 상태
    }

    public void markMissionAsFailed(int personalMissionId) {
        updateMissionStatus(personalMissionId, 2); // 실패 상태
    }

    private void updateMissionStatus(int personalMissionId, int status) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        missionMapper.updateMonthlyMissionStatus(personalMissionId, status);
    }
}
