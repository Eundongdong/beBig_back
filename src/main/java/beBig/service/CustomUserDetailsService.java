package beBig.service;

import beBig.mapper.UserMapper;
import beBig.vo.UserVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;  // DB에서 사용자 정보 조회

    @Override
    public CustomUserDetails loadUserByUsername(String userLoginId) throws UsernameNotFoundException {
        UserVo user = userMapper.findByUserLoginId(userLoginId);
        log.info("loadUserByUsername: {}", user);

        if (user == null) {
            throw new UsernameNotFoundException("유저를 찾지 못했습니다: " + userLoginId);
        }

        return new CustomUserDetails(
                user.getUserId(),
                user.getUserLoginId(),
                user.getUserPassword() // 데이터베이스에서 조회된 인코딩된 비밀번호
        );
    }
}
