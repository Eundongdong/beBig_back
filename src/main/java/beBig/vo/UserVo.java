package beBig.vo;

import lombok.Data;

import java.sql.Date;
import java.util.List;

@Data
public class UserVo {
    private Long userId;
    private int finTypeCode;
    private int userBadgeCode;
    private String userLoginId;
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
    private int userVisibility; // 0 = 비공개 , 1 = 공개
    private String refreshToken;

    private FinTypeVo finType;    // fin_type_code
    private BadgeVo badge;        // badge_code
    private List<PostVo> posts;   // user가 작성한 게시글
    private List<LikeHitsVo> likeHits;   // user가 누른 좋아요 목록
    private List<PersonalDailyMissionVo> dailyMissions;
    private PersonalMonthlyMissionVo monthlyMission;

    //메서드 추가 Date-> 나이 가져오는거
}
