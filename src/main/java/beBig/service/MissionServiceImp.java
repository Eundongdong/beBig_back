package beBig.service;


import beBig.dto.response.DailyMissionResponseDto;
import beBig.dto.response.MonthlyMissionResponseDto;
import beBig.mapper.MissionMapper;
import beBig.vo.MissionVo;
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
    private final MissionMapper missionMapper;

    @Autowired
    public MissionServiceImp(SqlSessionTemplate sqlSessionTemplate, MissionMapper missionMapper) {
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.missionMapper = missionMapper;
    }


    //월간미션
    @Override
    public MonthlyMissionResponseDto showMonthlyMission(long userId) {
        MissionMapper missonMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        MonthlyMissionResponseDto monthlyMissionResponseDto = missionMapper.getPersonalMonthlyMission(userId);
        return monthlyMissionResponseDto;
    }


    @Override
    public List<DailyMissionResponseDto> showDailyMission(long userId) {
        MissionMapper missonMapper = sqlSessionTemplate.getMapper(MissionMapper.class);

        return missionMapper.getPersonalDailyMission(userId);
    }

    @Override
    public void completeMonthlyMission(long personalMissionId) {
        MissionMapper missonMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        missionMapper.completeMonthlyMission(personalMissionId);
    }

    @Override
    public void completeDailyMission(long personalMissionId) {
        MissionMapper missonMapper = sqlSessionTemplate.getMapper(MissionMapper.class);
        missionMapper.completeDailyMonthlyMission(personalMissionId);
    }
}
