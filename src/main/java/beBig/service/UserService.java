package beBig.service;

import beBig.form.UserForm;
import beBig.mapper.UserMapper;
import beBig.vo.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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


    // 회원가입 서비스
    public void registerUser(UserForm userForm) throws Exception {
        // String을 Date로 변환 (java.time.LocalDate 사용)
  
    //로그인
    public boolean login(String userId, String rawPassword) {
        UserVo user = userMapper.findByUserId(userId);
        if (user == null) return false;

        // 비밀번호 비교 (입력한 비밀번호와 저장된 암호화된 비밀번호 비교)
        return passwordEncoder.matches(rawPassword, user.getUserPassword());
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
        // 사용자 정보 저장
        userMapper.insert(user);
        log.info("user 저장성공");
        log.info("user의 이름은 : " + user.getUserName());
        log.info(("user의 password는 : " + encryptedPassword));
    }

    public UserVo findByUserId(String userId) throws Exception {
        return userMapper.findByUserId(userId);
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
    public boolean updatePasswordByEmail(String userId, String name, String email) {
        Map<String, Object> params = new HashMap<>();
        String temporaryPassword = generateTemporaryPassword();
        params.put("userId", userId);
        params.put("name", name);
        params.put("email", email);
        params.put("userPassword", temporaryPassword); // 새 비밀번호 추가

        // 비밀번호 업데이트
        userMapper.updatePasswordByUserIdAndEmail(params);

        sendEmail(email, temporaryPassword); // 이메일로 임시 비밀번호 전송
        return true;

    }
    // 이메일 전송
    public void sendEmail(String toEmail, String tempPassword) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("yonggwon9941@gmail.com", "vpxo nprm uhio lkxf");
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("yonggwon9941@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your Temporary Password");
            message.setText("Here is your temporary password: " + tempPassword);

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

}
