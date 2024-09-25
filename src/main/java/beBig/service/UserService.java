package beBig.service;

import beBig.form.UserForm;
import beBig.mapper.UserMapper;
import beBig.vo.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

@Service
@Slf4j
public class UserService {

    private final UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    //유저등록(회원가입)
    public void registerUser(UserForm userForm) throws Exception {
        String encryptedPassword = passwordEncoder.encode(userForm.getPassword());
        // 새로운 사용자 객체 생성
        UserVo user = new UserVo();
        user.setUserName(userForm.getName());
        user.setUserNickname(userForm.getNickname());
        user.setUserId(userForm.getUserId());
        user.setUserPassword(encryptedPassword);// 비밀번호 암호화 필요
        user.setUserEmail(userForm.getEmail());
        user.setUserGender(userForm.isGender());
        user.setUserBirth(userForm.getBirth());
        user.setUserFinTypeCode(0);
        user.setUserBadgeCode(0);
        user.setUserLoginType(userForm.getUserLoginType());
        // 사용자 정보 저장
        userMapper.insert(user);
        log.info("user 저장성공");
        log.info("user의 이름은 : " + user.getUserName());
        log.info(("user의 password는 : " + encryptedPassword));
    }

    // 임시 비밀번호 생성
    private String generateTemporaryPassword() {
        int length = 10;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder tempPassword = new StringBuilder();

        //랜덤 비밀번호 설정
        for (int i = 0; i < length; i++) {
            tempPassword.append(chars.charAt(random.nextInt(chars.length())));
        }

        return tempPassword.toString();
    }

    // 임시 비밀번호로 사용자 비밀번호 업데이트
    public boolean updatePasswordByEmail(String userId, String name, String email) {
        Map<String, Object> params = new HashMap<>();
        String temporaryPassword = generateTemporaryPassword();
        // 암호화
        String encodedPassword = passwordEncoder.encode(temporaryPassword);

        params.put("userId", userId);
        params.put("name", name);
        params.put("email", email);
        params.put("userPassword", encodedPassword);

        try {
            // 비밀번호 업데이트
            int updatedRows = userMapper.updatePasswordByUserIdAndEmail(params);

            // 업데이트가 성공한 경우에만 이메일 전송
            if (updatedRows > 0) {
                sendEmail(email, temporaryPassword);
                return true;
            } else {
                // 유효하지 않은 정보일 경우 처리
                log.info("업데이트 실패");
                return false; // 실패했음을 나타냄
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false; // 오류 발생 시 false 반환
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
            System.out.println("Temporary password email sent to: " + toEmail);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //아이디 중복체크
    public boolean isUserIdDuplicated(String userId) {
        return userMapper.isUserIdDuplicated(userId);
    }

    //아이디 찾기
    public String findUserIdByNameAndEmail(String name, String email) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("email", email);

        String userId = userMapper.findUserIdByNameAndEmail(params);

        if (userId != null && userId.length() > 4) {
            // 아이디의 마지막 4자리를 별표로 마스킹
            String maskedUserId = userId.substring(0, userId.length() - 4) + "****";
            return maskedUserId;
        }
        return null; // 사용자 아이디가 없거나 너무 짧은 경우
    }

    public boolean findByUserIdAndLoginType(String email, String userLoginType) {
        return userMapper.findByUserIdAndLoginType(email, userLoginType);
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
