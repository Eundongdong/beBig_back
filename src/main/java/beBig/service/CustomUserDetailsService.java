package beBig.service;

import beBig.mapper.UserMapper;
import beBig.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;  // DB에서 사용자 정보 조회

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserVo user = userMapper.findByUserId(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        // UserDetails를 반환하는 org.springframework.security.core.userdetails.User 객체 생성
        return new org.springframework.security.core.userdetails.User(user.getUserId(), user.getUserPassword(),
                new ArrayList<>());
    }
}

