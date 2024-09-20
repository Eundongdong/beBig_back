package beBig.vo;

import java.sql.Date;
import java.util.List;

public class UserVo {
    private Long userNo;
    private int userFinTypeCode;
    private int userBadgeCode;
    private String userId;
    private String userName;
    private String userNickname;
    private String userPassword;
    private String userEmail;
    private boolean userGender;
    private Date userBirth; // 추후 유틸 클래스를 통해 Date로 변환예정
    private int userAgeRange;
    private String userLoginType;
    private int userMissionCurrentMonthScore; //현재월별점수
    private int userMissionLastMonthScore; // 지난월별점수
    private String userConnectedId;
    private String userIntro;
    private int userSalary;

    private FinTypeVo finType;    // fin_type_code
    private BadgeVo badge;        // badge_code
    private List<PostVo> posts;   // user가 작성한 게시글
    private List<LikeHitsVo> likeHits;   // user가 누른 좋아요 목록
    private List<PersonalDailyMissionVo> dailyMissions;
    private PersonalMonthlyMissionVo monthlyMission;
}
