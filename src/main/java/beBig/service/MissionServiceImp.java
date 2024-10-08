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

    //월간미션
    @Override
    public MonthlyMissionResponseDto showMonthlyMission(long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        MonthlyMissionResponseDto monthlyMissionResponseDto = missionMapper.getPersonalMonthlyMission(userId);
        return monthlyMissionResponseDto;
    }

    @Override
    public List<DailyMissionResponseDto> showDailyMission(long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);

        return missionMapper.getPersonalDailyMission(userId);
    }

    @Override
    public void completeMonthlyMission(long personalMissionId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        missionMapper.completeMonthlyMission(personalMissionId, 1);
    }

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

    @Override
    public int findSalary(long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        return missionMapper.findSalaryByUserId(userId);
    }

    //점수계산
    @Override
    public int updateScore(long userId, int amount) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        //월별점수가져옴
        int currentScore = missionMapper.findCurrentMissionMonthScoreByUserId(userId) + amount;
        missionMapper.updateCurrentMissionMonthScoreByUserId(userId, currentScore);
        return currentScore;
    }

    @Override
    public long findIsCompleted(long personalMissionId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        return missionMapper.findMissionIsCompletedByPersonalMissionId(personalMissionId);
    }

    @Override
    public int findCurrentMonthScore(long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        return missionMapper.findCurrentMissionMonthScoreByUserId(userId);
    }

    // 전체 사용자 업데이트
    public void assignDailyMission() {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);  // Mapper를 동적으로 가져옴
        List<Long> userIds = missionMapper.findAllUsersWithDailyMissions(); // 모든 사용자 ID 조회

        for (Long userId : userIds) {
            updateDailyMissionForUser(userId); // 미션 갱신
        }
    }

    // 사용자에 대한 미션 갱신
    public void updateDailyMissionForUser(Long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);  // Mapper를 동적으로 가져옴
        // type이 2인 미션을 랜덤으로 3개 가져옴
        List<Integer> dailyMissions = missionMapper.findRandomMissionsByType(2, 3);

        // 기존 personal_daily_mission_id를 가져옴
        List<Integer> existingMissionIds = missionMapper.findExistingDailyMissions(userId);

        // 기존 미션이 있을 경우 mission_id, 완료여부 0으로 업데이트
        if (!existingMissionIds.isEmpty()) {
            for (int i = 0; i < Math.min(dailyMissions.size(), existingMissionIds.size()); i++) {
                missionMapper.updateDailyMission(userId, existingMissionIds.get(i), dailyMissions.get(i)); // 각 미션 ID를 업데이트
            }
        }
    }

    // 신규 미션 추가
    public void addDailyMissions(Long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);  // Mapper를 동적으로 가져옴
        // 자산 상태 체크
        boolean isAssetAndSurveyLoaded = missionMapper.countUserAssetStatus(userId) > 0;

        // 자산과 설문조사가 모두 완료된 경우
        if (isAssetAndSurveyLoaded) {
            // 기존 미션이 있는지 확인하기 위해 count 쿼리 사용
            int existingMissionCount = missionMapper.findExistingDailyMissionsCount(userId); // 새로운 메서드 추가

            // 기존 미션이 없을 경우
            if (existingMissionCount == 0) {
                List<Integer> dailyMissions = missionMapper.findRandomMissionsByType(2, 3);
                // 신규 미션 삽입
                for (int dailyMission : dailyMissions) {
                    missionMapper.insertDailyMission(userId, dailyMission);
                }
                generateOrUpdateMonthlyMission(userId);
            }
        }
    }

    // 월간 미션 생성 및 업데이트 로직 추가
    public void generateOrUpdateMonthlyMission(Long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        // 기존 월간 미션이 있는지 확인
        log.info("generateOr어쩌고 호출완료");
        int existingMonthlyMissionCount = missionMapper.countPersonalMonthlyMission(userId);

        if (existingMonthlyMissionCount == 0) {
            // 신규 미션을 생성해야 할 경우 첫 번째 미션을 할당
            missionMapper.insertMonthlyMission(userId, 1);
            log.info("1번미션 할당");
        } else {
            // 기존 미션이 있을 경우 업데이트 (missionId % 6) + 1
            int currentMissionId = missionMapper.getCurrentMonthlyMissionId(userId);
            int nextMissionId = (currentMissionId % 6) + 1;
            missionMapper.updateMonthlyMission(userId, nextMissionId);
        }
    }

    public void checkMonthlyMissionCompletion(Long userId) {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        // 사용자에게 할당된 월간 미션 ID를 가져옴
        PersonalMonthlyMissionVo currentMission = missionMapper.getCurrentMonthlyMission(userId);

        // 미션 ID에 따라 성공 여부 판단
        switch (currentMission.getMissionId()) {
            case 1: // 새로운 예/적금 가입
                if (missionMapper.countSavingsAccounts(userId) > 0) {
                    missionMapper.completeMonthlyMission(currentMission.getPersonalMonthlyMissionId(), 1); // 성공(1)
                } else {
                    missionMapper.updateMonthlyMissionStatus(currentMission.getPersonalMonthlyMissionId(), 0); // 진행중(0)
                }
                break;
            case 2: // 커뮤니티 글 4개 이상 작성
                if (missionMapper.countCommunityPosts(userId) >= 4) {
                    missionMapper.completeMonthlyMission(currentMission.getPersonalMonthlyMissionId(), 1); // 성공(1)
                } else {
                    missionMapper.updateMonthlyMissionStatus(currentMission.getPersonalMonthlyMissionId(), 0); // 진행중(0)
                }
                break;
            case 3: // 좋아요 50개 이상 받기
                if (missionMapper.countPostLikesInMonth(userId) >= 50) {
                    missionMapper.completeMonthlyMission(currentMission.getPersonalMonthlyMissionId(), 1); // 성공(1)
                } else {
                    missionMapper.updateMonthlyMissionStatus(currentMission.getPersonalMonthlyMissionId(), 0); // 진행중(0)
                }
                break;
            case 4: // 소비 n원 줄이기
                if (missionMapper.calculateSpendingDifference(userId) < 0) {
                    missionMapper.completeMonthlyMission(currentMission.getPersonalMonthlyMissionId(), 1); // 성공(1)
                } else {
                    missionMapper.updateMonthlyMissionStatus(currentMission.getPersonalMonthlyMissionId(), 0); // 진행중(0)
                }
                break;
            case 5: // n원 저축하기
                if (missionMapper.calculateSavingDifference(userId) < 0) {
                    missionMapper.completeMonthlyMission(currentMission.getPersonalMonthlyMissionId(), 1); // 성공(1)
                } else {
                    missionMapper.updateMonthlyMissionStatus(currentMission.getPersonalMonthlyMissionId(), 0); // 진행중(0)
                }
                break;
            case 6: // 매일 데일리 미션 수행
                if (missionMapper.countCompletedDailyMissions(userId) > 0) {
                    missionMapper.updateMonthlyMissionStatus(currentMission.getPersonalMonthlyMissionId(), 0); // 진행중(0)
                } else {
                    missionMapper.updateMonthlyMissionStatus(currentMission.getPersonalMonthlyMissionId(), 2); // 실패(2)
                }
                break;
            default:
                log.warn("Unknown mission ID: " + currentMission.getMissionId());
        }
    }

    // 전체 사용자에 대한 월간 미션 갱신 및 검증 -> batch에서 사용
    public void updateAllMonthlyMissions() {
        MissionMapper missionMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        List<Long> userIds = missionMapper.findAllUsersWithMonthlyMissions();

        for (Long userId : userIds) {
            generateOrUpdateMonthlyMission(userId); // 월간 미션 갱신
            checkMonthlyMissionCompletion(userId); // 미션 성공 여부 확인
        }
    }
}
