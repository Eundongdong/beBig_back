package beBig.service;

import beBig.form.UserForm;
import beBig.mapper.UserMapper;
import beBig.vo.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

@Service
@Slf4j
public class UserServiceImp implements UserService {

    private final SqlSessionTemplate sqlSessionTemplate;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImp(SqlSessionTemplate sqlSessionTemplate, PasswordEncoder passwordEncoder) {
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    // 유저 등록(회원가입)
    @Override
    public void registerUser(UserForm userForm) throws Exception {
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        String encryptedPassword = passwordEncoder.encode(userForm.getPassword());
        // 새로운 사용자 객체 생성
        UserVo user = new UserVo();
        user.setUserName(userForm.getName());
        user.setUserNickname(userForm.getNickname());
        user.setUserId(userForm.getUserId());
        user.setUserPassword(encryptedPassword); // 비밀번호 암호화 필요
        user.setUserEmail(userForm.getEmail());
        user.setUserGender(userForm.isGender());
        user.setUserBirth(userForm.getBirth());
        user.setUserFinTypeCode(0);
        user.setUserBadgeCode(0);
        // 사용자 정보 저장
        userMapper.insert(user);
        log.info("user 저장성공: {}", user.getUserName());
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
    public boolean updatePasswordByEmail(String userId, String name, String email) {
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        String temporaryPassword = generateTemporaryPassword();
        String encodedPassword = passwordEncoder.encode(temporaryPassword);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("name", name);
        params.put("email", email);
        params.put("userPassword", encodedPassword);

        try {
            int updatedRows = userMapper.updatePasswordByUserIdAndEmail(params);
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

    @Value("${mail.smtp.host}") private String smtpHost;
    @Value("${mail.smtp.port}") private String smtpPort;
    @Value("${mail.smtp.auth}") private String smtpAuth;
    @Value("${mail.smtp.starttls.enable}") private String starttlsEnable;
    @Value("${mail.username}") private String emailUsername;
    @Value("${mail.password}") private String emailPassword;
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
    public boolean isUserIdDuplicated(String userId) {
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        return userMapper.isUserIdDuplicated(userId);
    }

    //아이디 찾기
    @Override
    public String findUserIdByNameAndEmail(String name, String email) {
        UserMapper userMapper = sqlSessionTemplate.getMapper(UserMapper.class);
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("email", email);

        String userId = userMapper.findUserIdByNameAndEmail(params);

        if (userId != null && userId.length() > 4) {
            return userId.substring(0, userId.length() - 4) + "****";
        }
        return null;
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