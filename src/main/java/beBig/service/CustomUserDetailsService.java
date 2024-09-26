package beBig.service;

import beBig.mapper.UserMapper;
import beBig.vo.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;  // DB에서 사용자 정보 조회

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // UserVo 객체를 DB에서 조회
        UserVo user = userMapper.findByUserLoginId(username);

        if (user == null) {
            throw new UsernameNotFoundException("유저를 찾지 못했습니다: " + username);
        }

        // 사용자의 권한 설정 (예시: ROLE_USER로 기본 권한을 부여)
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // UserDetails를 반환하는 org.springframework.security.core.userdetails.User 객체 생성
        return new org.springframework.security.core.userdetails.User(
                user.getUserLoginId(),
                user.getUserPassword(),
                authorities            // 사용자 권한
        );
    }
}
