package beBig.service;

import beBig.dto.UserDto;
import beBig.dto.response.FinInfoResponseDto;
import beBig.mapper.UserMapper;
import beBig.vo.FinTestVo;
import beBig.vo.FinTypeVo;
import beBig.vo.UserVo;
import beBig.vo.UtilVo;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
public class UserServiceImp implements UserService {

    private final SqlSessionTemplate sqlSessionTemplate;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImp(SqlSessionTemplate sqlSessionTemplate, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    // 유저 등록(회원가입)
    @Override
    public void registerUser(UserDto userDto) throws Exception {
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        String encryptedPassword = passwordEncoder.encode(userDto.getPassword());

        // 새로운 사용자 객체 생성
        UserVo user = new UserVo();
        user.setUserName(userDto.getName());
        user.setUserNickname(userDto.getNickname());
        user.setUserLoginId(userDto.getUserLoginId());
        user.setUserPassword(encryptedPassword); // 비밀번호 암호화
        user.setUserEmail(userDto.getEmail());
        user.setUserGender(userDto.isGender());
        user.setUserBirth(userDto.getBirth());
        user.setFinTypeCode(0);
        user.setUserBadgeCode(0);
        user.setUserLoginType(userDto.getUserLoginType());

        // 나이를 계산해 연령대 저장
        int age = calculateAge(userDto.getBirth()); // 나이 계산
        user.setUserAgeRange(age - age%10); // 연령대 저장

        // 사용자 정보 저장
        userMapper.insert(user);
        log.info("user 저장성공: {}", user.getUserName());
    }

    // 나이를 계산하는 메서드
    private int calculateAge(Date birthDate) {
        LocalDate birth = birthDate.toLocalDate();
        LocalDate now = LocalDate.now();
        return Period.between(birth, now).getYears(); // 나이 계산
    }

    // 임시 비밀번호 생성
    private String generateTemporaryPassword() {
        int length = 10;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder tempPassword = new StringBuilder();

        for (int i = 0; i < length; i++) {
            tempPassword.append(chars.charAt(random.nextInt(chars.length())));
        }

        return tempPassword.toString();
    }

    // 임시 비밀번호로 사용자 비밀번호 업데이트
    @Override
    public boolean updatePasswordByEmail(String userLoginId, String name, String email) {
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        String temporaryPassword = generateTemporaryPassword();
        String encodedPassword = passwordEncoder.encode(temporaryPassword);

        Map<String, Object> params = new HashMap<>();
        params.put("userLoginId", userLoginId);
        params.put("name", name);
        params.put("email", email);
        params.put("userPassword", encodedPassword);

        try {
            int updatedRows = userMapper.updatePasswordByUserLoginIdAndEmail(params);
            // 업데이트가 성공한 경우에만 이메일 전송
            if (updatedRows > 0) {
                sendEmail(email, temporaryPassword);
                return true;
            } else {
                log.info("업데이트 실패");
                return false;
            }
        } catch (Exception e) {
            log.error("비밀번호 업데이트 중 오류", e);
            return false;
        }
    }

    @Value("${mail.smtp.host}")
    private String smtpHost;
    @Value("${mail.smtp.port}")
    private String smtpPort;
    @Value("${mail.smtp.auth}")
    private String smtpAuth;
    @Value("${mail.smtp.starttls.enable}")
    private String starttlsEnable;
    @Value("${mail.username}")
    private String emailUsername;
    @Value("${mail.password}")
    private String emailPassword;

    // 이메일 전송
    public void sendEmail(String toEmail, String tempPassword) {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", starttlsEnable);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUsername, emailPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("yonggwon9941@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("임시 비밀번호 발급");
            message.setText("임시 비밀번호 : " + tempPassword + "\n비밀번호를 변경해주세요.");

            Transport.send(message);
            log.info("임시 비밀번호 이메일 발송: {}", toEmail);

        } catch (Exception e) {
            log.error("이메일 발송 중 오류", e);
        }
    }

    //아이디 중복체크
    @Override
    public boolean isUserLoginIdDuplicated(String userLoginId) {
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        int count = userMapper.isUserLoginIdDuplicated(userLoginId);
        return count > 0;
    }

    //아이디 찾기
    @Override
    public String findUserLoginIdByNameAndEmail(String name, String email) {
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("email", email);
        log.info(params.toString());
        String userId = userMapper.findUserLoginIdByNameAndEmail(params);
        log.info(userId);
        if (userId != null) {
            if (userId.length() > 4) {
                return userId.substring(0, userId.length() - 4) + "****";
            }
            return userId.substring(0, userId.length() - 1) + "*";
        }
        return null;
    }

    // 소셜 회원가입유무 확인
    @Override
    public boolean findByEmailAndLoginType(String email, String userLoginType) {
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        return userMapper.findByEmailAndLoginType(email, userLoginType);
    }

    // 약관 조회 메서드 구현
    @Override
    public List<UtilVo> getUtilTerms() {
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        return userMapper.getUtilTerms();
    }

    @Override
    public Long findUserIdByKakaoId(String kakaoId) {
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        return userMapper.getUserIdByKaKaoId(kakaoId);
    }

    @Override
    // db에서 refreshToken 지우기 (로그아웃시)
    public void removeRefreshToken(String refreshToken) {
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        userMapper.clearRefreshTokenRT(refreshToken);
        userMapper.clearRefreshTokenUser(refreshToken);
    }

    @Override
    public Long findUserIdByUserLoginId(String userLoginId) {
        return userMapper.findUserIdByUserLoginId(userLoginId);
    }

    @Override
    public void deleteRefreshTokenBeforeLogin(Long userId) {
        if (checkIfRefreshTokenExistsByUserId(userId)) {
            userMapper.clearRefreshTokenByUserId(userId);
        }
    }

    @Override
    public boolean checkIfRefreshTokenExistsByUserId(Long userId) {
        return userMapper.countRefreshTokenByUserId(userId) > 0;
    }

    @Override
    public boolean isEmailDuplicate(String email) {
        int duplicateEmail = userMapper.checkDuplicateEmail(email);
        if (duplicateEmail > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isNicknameDuplicate(String nickname) {
        int duplicateNickname = userMapper.checkDuplicateNickname(nickname);
        if (duplicateNickname > 0) {
            return true;
        }
        return false;
    }

    @Override
    public void updateUserAges() {
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        List<UserVo> users = userMapper.getAllUsers();

        for (UserVo user : users) {
            Date birthDate = user.getUserBirth();
            if (birthDate != null) { // null 체크
                LocalDate localBirthDate = (birthDate).toLocalDate();
                int birthYear = localBirthDate.getYear();
                int age = currentYear - birthYear; // 나이 계산

                // 나이에 따라 나이 범위 설정
                int ageRange = (age / 10) * 10;


                userMapper.updateUserAgeRange(user.getUserId(), ageRange);
                log.info("updated :" + user.getUserLoginId() + " - age range: " + ageRange);
            }
        }
    }


    //    public UserVo findByUserId(String userId) throws Exception {
//        return userMapper.findByUserId(userId);
//    }

//    //로그인
//    public boolean login(String userId, String rawPassword) {
//        UserVo user = userMapper.findByUserId(userId);
//        if (user == null) return false;
//
//        // 비밀번호 비교 (입력한 비밀번호와 저장된 암호화된 비밀번호 비교)
//        return passwordEncoder.matches(rawPassword, user.getUserPassword());
//    }

}
