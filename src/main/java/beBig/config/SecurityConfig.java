package beBig.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.filter.CharacterEncodingFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Log4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {

//    //문자셋 필터 - 한글이 제대로 안보내지는 이슈가 있어서 인코딩 필터를 등록!
//    public CharacterEncodingFilter encodingFilter() {
//        CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
//        encodingFilter.setEncoding("UTF-8");
//        encodingFilter.setForceEncoding(true);
//        return encodingFilter;
//    }

}
