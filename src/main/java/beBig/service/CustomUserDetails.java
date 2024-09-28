package beBig.service;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    // userId 반환 메서드 추가
    @Getter
    private final Long userId; // Long userId 추가
    private final String userLoginId;
    private final String password;

    public CustomUserDetails(Long userId, String userLoginId, String password) {
        this.userId = userId; // Long userId 초기화
        this.userLoginId = userLoginId;
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userLoginId; // 여기서는 userId 대신 username을 사용할 수도 있음
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
