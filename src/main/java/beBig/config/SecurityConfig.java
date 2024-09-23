package beBig.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.filter.CharacterEncodingFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Log4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    //문자셋 필터 - 한글이 제대로 안보내지는 이슈가 있어서 인코딩 필터를 등록!
    public CharacterEncodingFilter encodingFilter() {
        CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.setForceEncoding(true);
        return encodingFilter;
    }


    //로그인페이지 설정
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()  // 필요에 따라 CSRF 보호를 비활성화
                .authorizeRequests()
                .antMatchers("/login", "/signup", "/resources/**").permitAll()  // 로그인 및 회원가입, 리소스 접근 허용
                .anyRequest().authenticated()  // 그 외의 요청은 인증 필요
                .and()
                .formLogin()
                .loginPage("/login")  // 커스텀 로그인 페이지 경로 설정
                .defaultSuccessUrl("/home", true)  // 로그인 성공 후 리다이렉트 경로
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")  // 로그아웃 후 리다이렉트 경로
                .permitAll();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
